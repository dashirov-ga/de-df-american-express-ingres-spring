package ly.generalassemb.de.american.express.ingress.config;

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.http.OkHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.squareup.okhttp.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class SnowplowTrackerConfig {
    private static final AtomicInteger eventCounter = new AtomicInteger(0);

    @Value("${monitoring.snowplow.url}")
    private URL url;
    @Value("${monitoring.snowplow.namespace}")
    private String namespace;
    @Value("${monitoring.snowplow.application}")
    private String application;

    private Logger LOGGER = LoggerFactory.getLogger(SnowplowTrackerConfig.class);
    @Bean
    public Tracker snowplowTracker(){
        Tracker tracker = new Tracker.TrackerBuilder(BatchEmitter.builder()
                .requestCallback(
                        new RequestCallback() {
                            @Override
                            public void onSuccess(int i) {
                                int events_left = eventCounter.addAndGet(-1 * i);
                            }

                            @Override
                            public void onFailure(int i, List<TrackerPayload> list) {
                                int events_left = eventCounter.addAndGet(-1 * (i + list.size()));
                                LOGGER.error(list.toString());
                            }
                        }
                )
                .httpClientAdapter(OkHttpClientAdapter.builder()
                        .url(url.toString())
                        .httpClient(new OkHttpClient())
                        .build())
                .build(), namespace,
                application)
                .base64(false)
                .platform(DevicePlatform.ServerSideApp)
                .build();
        return tracker;

    }

}
