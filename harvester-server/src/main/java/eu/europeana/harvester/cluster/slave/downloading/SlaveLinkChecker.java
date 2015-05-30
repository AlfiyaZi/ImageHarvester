package eu.europeana.harvester.cluster.slave.downloading;

import com.ning.http.client.*;
import eu.europeana.harvester.cluster.domain.messages.RetrieveUrl;
import eu.europeana.harvester.domain.DocumentReferenceTaskType;
import eu.europeana.harvester.httpclient.response.HttpRetrieveResponse;
import eu.europeana.harvester.httpclient.response.ResponseState;
import eu.europeana.harvester.utils.NetUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SlaveLinkChecker {

    private org.apache.logging.log4j.Logger LOG;

    public SlaveLinkChecker(final Logger LOG) {
        this.LOG = LOG;
    }

    public void downloadAndStoreInHttpRetrievResponse(final HttpRetrieveResponse httpRetrieveResponse, final RetrieveUrl task) {

        if ((task.getDocumentReferenceTask().getTaskType() != DocumentReferenceTaskType.CHECK_LINK)) {
            throw new IllegalArgumentException("The link checker can handle only link checking downloads. Cannot handle "+task.getDocumentReferenceTask().getTaskType());
        }

        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder()
                .setMaxRedirects(task.getHttpRetrieveConfig().getMaxNrOfRedirects())
                .setFollowRedirect(true)
                .setConnectTimeout(100000)
                .setAcceptAnyCertificate(true)
                .setMaxRequestRetry(3)
                .build());


        httpRetrieveResponse.setState(ResponseState.PROCESSING);
        httpRetrieveResponse.setRetrievalDurationInMilliSecs(0l);
        final long connectionSetupStartTimestamp = System.currentTimeMillis();

        final ListenableFuture<Integer> downloadListener = asyncHttpClient.prepareGet(task.getUrl()).execute(new AsyncHandler<Integer>() {

            @Override
            public STATE onStatusReceived(HttpResponseStatus status) throws Exception {

                final long connectionSetupDurationInMillis = System.currentTimeMillis() - connectionSetupStartTimestamp;
                httpRetrieveResponse.setSocketConnectToDownloadStartDurationInMilliSecs(connectionSetupDurationInMillis);
                httpRetrieveResponse.setCheckingDurationInMilliSecs(connectionSetupDurationInMillis);

                httpRetrieveResponse.setUrl(new URL(task.getUrl()));
                httpRetrieveResponse.setSourceIp(NetUtils.ipOfUrl(task.getUrl()));

                if (connectionSetupDurationInMillis > task.getHttpRetrieveConfig().getConnectionTimeoutInMillis()) {
                    /* Initial connection setup time longer than threshold. */
                    httpRetrieveResponse.setState(ResponseState.FINISHED_TIME_LIMIT);
                    httpRetrieveResponse.setLog("Link checking aborted as connection setup duration " + connectionSetupDurationInMillis + " ms was greater than maximum configured  " + task.getHttpRetrieveConfig().getConnectionTimeoutInMillis() + " ms");
                    return STATE.ABORT;
                }


                if (connectionSetupDurationInMillis > task.getHttpRetrieveConfig().getTerminationThresholdReadPerSecondInBytes()) {
                    /* Initial connection setup time longer than threshold. */
                    httpRetrieveResponse.setState(ResponseState.FINISHED_TIME_LIMIT);
                    httpRetrieveResponse.setLog("Link checking aborted as connection setup duration " + connectionSetupDurationInMillis + " ms was greater than maximum configured total download duration threshold" + task.getHttpRetrieveConfig().getTerminationThresholdTimeLimit() + " ms");
                    return STATE.ABORT;
                }
                    /* We don't care what kind of status code it has at this moment as we will decide what to
                     * do on it only after the response headers have been received.
                     */
                httpRetrieveResponse.setHttpResponseCode(status.getStatusCode());

                return STATE.CONTINUE;
            }

            @Override
            public STATE onHeadersReceived(HttpResponseHeaders downloadResponseHeaders) throws Exception {

                final long downloadDurationInMillis = System.currentTimeMillis() - connectionSetupStartTimestamp;
                httpRetrieveResponse.setRetrievalDurationInMilliSecs(downloadDurationInMillis);

                    /* Collect the response headers */
                for (final Map.Entry<String, List<String>> entry : downloadResponseHeaders.getHeaders()) {
                    for (final String header : entry.getValue()) {
                        httpRetrieveResponse.addHttpResponseHeaders(entry.getKey(), header);
                    }
                }

                /** We terminate the connection in case of HTTP error only after we collect the response headers */
                if (httpRetrieveResponse.getHttpResponseCode() >= 400) {
                    httpRetrieveResponse.setState(ResponseState.ERROR);
                    httpRetrieveResponse.setLog("HTTP >=400 code received. Connection aborted after response headers collected.");
                    return STATE.ABORT;
                }

                httpRetrieveResponse.setState(ResponseState.COMPLETED);
                return STATE.ABORT;
            }

            @Override
            public Integer onCompleted() throws Exception {
                cleanup(httpRetrieveResponse, asyncHttpClient, httpRetrieveResponse.getException());
                return 0;
            }


            @Override
            public void onThrowable(Throwable e) {

                // Check if the tim threshold limit was exceeded & save that information.
                final long downloadDurationInMillis = System.currentTimeMillis() - connectionSetupStartTimestamp;
                if (downloadDurationInMillis > task.getHttpRetrieveConfig().getTerminationThresholdTimeLimit().getMillis()) {
                    /* Download duration longer than threshold. */
                    httpRetrieveResponse.setState(ResponseState.FINISHED_TIME_LIMIT);
                    httpRetrieveResponse.setLog("Link checking aborted as it's duration " + downloadDurationInMillis + " ms was greater than maximum configured  " + task.getHttpRetrieveConfig().getTerminationThresholdTimeLimit().getMillis() + " ms");
                }

                // Check if it was aborted because of conditional download with with same headers.
                if (httpRetrieveResponse.getState() == ResponseState.COMPLETED) {
                    // We don't set any exception as the download was aborted for a legitimate reason.
                    cleanup(httpRetrieveResponse, asyncHttpClient, httpRetrieveResponse.getException());
                }
                else {
                    // We set the exception as the download was aborted because of a problem.
                    cleanup(httpRetrieveResponse, asyncHttpClient, e);
                }
            }

            @Override
            public STATE onBodyPartReceived(HttpResponseBodyPart httpResponseBodyPart) throws Exception {
                return STATE.ABORT;
            }
        });

        try {
            Integer r = downloadListener.get(1, TimeUnit.DAYS /* This timeout should never be reached. There are other timeouts used internally that will expire much quicker. */);
            LOG.debug("Download finished with status {}", r);

        } catch (Exception e) {
            cleanup(httpRetrieveResponse, asyncHttpClient, e);
        } finally {
            cleanup(httpRetrieveResponse, asyncHttpClient, httpRetrieveResponse.getException());
        }
    }

    private void cleanup(final HttpRetrieveResponse httpRetrieveResponse, final AsyncHttpClient asyncHttpClient, final Throwable e) {
        if (httpRetrieveResponse != null) httpRetrieveResponse.setException(e);
        try {
            if (httpRetrieveResponse != null) httpRetrieveResponse.close();
        } catch (IOException e1) {
            LOG.error("Failed to close the response, caused by : " + e1.getMessage());
        }

        // if (asyncHttpClient != null && !asyncHttpClient.isClosed()) asyncHttpClient.close();
    }

}