package twitter4j;

import lombok.extern.slf4j.Slf4j;
import twitter4j.auth.Authorization;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * for twitter4j-4.0.5, "tweet_mode=extended" workaround<br>
 * <br>
 * You can add this class to your project and run java:
 * <pre>-Dtwitter4j.http.httpClient=twitter4j.WorkaroundLongTweetHttpClientImpl</pre>
 * or rename this to "twitter4j.AlternativeHttpClientImpl" if no twitter4j-http2-module.<br>
 * <br>
 * This is only one way to rewrite GET parameter in twitter4j without modify.<br>
 * If you need get "display_text_range" property, use {@link ConfigurationBuilder#setJSONStoreEnabled(boolean)} and {@link TwitterObjectFactory#getRawJSON(Object)}
 *
 * @see HttpClientFactory
 */
@Slf4j
public class WorkaroundLongTweetHttpClientImpl extends HttpClientImpl {
	private static final long serialVersionUID = -8943149293911924764L;

	static {
		//log.info("Using {} as HttpClient", WorkaroundLongTweetHttpClientImpl.class.getCanonicalName());
	}

	public WorkaroundLongTweetHttpClientImpl(final HttpClientConfiguration conf) {
		super(conf);
	}

	@Override
	public HttpResponse get(final String url, final HttpParameter[] parameters, final Authorization authorization,
			final HttpResponseListener listener) throws TwitterException {
		if (parameters != null) {
			HttpParameter[] newParameters = Arrays.copyOf(parameters, parameters.length + 1);
			newParameters[newParameters.length - 1] = new HttpParameter("tweet_mode", "extended");
			return new WorkAroundLongTweetHttpResponse(super.get(url, newParameters, authorization, listener));
		}else{
			return new WorkAroundLongTweetHttpResponse(super.get(url, null, authorization, listener));
		}

	}
}

/**
 * rewrite response
 */
class WorkAroundLongTweetHttpResponse extends HttpResponse {
	private final HttpResponse delegate;

	WorkAroundLongTweetHttpResponse(final HttpResponse delegate) {
		this.delegate = delegate;
	}

	private JSONObject convert(final JSONObject values) throws TwitterException {
		for (Iterator<String> iter = values.keys(); iter.hasNext();) {
			String key = iter.next();
			Object value = values.opt(key);
			if (value instanceof JSONObject) {
				convert((JSONObject) value);
			} else if (value instanceof JSONArray) {
				convert((JSONArray) value);
			}
		}
		if (values.has("full_text")) {
			try {
				values.put("text", values.remove("full_text"));
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
		}
		return values;
	}

	private JSONArray convert(final JSONArray values) throws TwitterException {
		for (int i = 0; i < values.length(); i++) {
			Object value = values.opt(i);
			if (value instanceof JSONArray) {
				convert((JSONArray) value);
			} else if (value instanceof JSONObject) {
				convert((JSONObject) value);
			}
		}
		return values;
	}

	@Override
	public JSONObject asJSONObject() throws TwitterException {
		return convert(super.asJSONObject());
	}

	@Override
	public JSONArray asJSONArray() throws TwitterException {
		return convert(super.asJSONArray());
	}

	@Override
	public int getStatusCode() {
		return this.delegate.getStatusCode();
	}

	@Override
	public String getResponseHeader(final String name) {
		return this.delegate.getResponseHeader(name);
	}

	@Override
	public int hashCode() {
		return this.delegate.hashCode();
	}

	@Override
	public Map<String, List<String>> getResponseHeaderFields() {
		return this.delegate.getResponseHeaderFields();
	}

	@Override
	public InputStream asStream() {
		return this.delegate.asStream();
	}

	@Override
	public String asString() throws TwitterException {
		return this.delegate.asString();
	}

	@Override
	public boolean equals(final Object obj) {
		return this.delegate.equals(obj);
	}

	@Override
	public Reader asReader() {
		return this.delegate.asReader();
	}

	@Override
	public void disconnect() throws IOException {
		this.delegate.disconnect();
	}

	@Override
	public String toString() {
		return this.delegate.toString();
	}
}