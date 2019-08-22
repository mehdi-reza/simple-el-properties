package properties.el;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesEl {

	final OrderedProperties raw;
	Properties elResolved = new Properties();

	private static final Pattern elMatch = Pattern.compile("(\\.*?(\\$\\{\\w+\\}))\\.*?");

	public static void main(String[] args) {
		new PropertiesEl().print();
	}

	public PropertiesEl() {
		raw=new OrderedProperties(PropertiesEl.class.getResourceAsStream("/input.properties"));
		processEl();
	}

	private BiFunction<Matcher, Entry<String, String>, String> resolver(final Properties props) {
		return (m, entry) -> {
			String input = entry.getValue();
			while (m.find()) {				
				String replaceWith = props.getProperty(m.group().substring(2, m.group().length() - 1));
				Objects.requireNonNull(replaceWith, "Could not find corresponding value for "+m.group());
				input = input.replace(m.group(), replaceWith);
			}
			return input;
		}; 
	}

	private void processEl() {
		BiFunction<Matcher, Entry<String, String>, String> _resolver = resolver(elResolved);		
		raw.entrySet().stream().sequential().forEach(e -> {
			Matcher m = elMatch.matcher(e.getValue());
			elResolved.put(e.getKey(), m.find() ? _resolver.apply(m.reset(e.getValue()), e) : e.getValue());
		});
	}

	private void print() {
		elResolved.entrySet().forEach(entry -> {
			System.out.println(entry.getKey()+" = "+entry.getValue());
		});
	}

	static class OrderedProperties {
		
		private LinkedHashMap<String, String> props = new LinkedHashMap<>();

		public OrderedProperties(InputStream input) {
			try (InputStreamReader is = new InputStreamReader(input)) {
				try(BufferedReader reader = new BufferedReader(is)) {
					String line = null;
					while((line = reader.readLine()) != null) {
						String[] lineSplit = line.split(" = ");
						props.put(lineSplit[0], lineSplit[1]);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			} catch (IOException e1) {
				throw new UncheckedIOException(e1);
			}
		}
		
		public Set<Entry<String, String>> entrySet() {
			return props.entrySet();
		}
	}
}
