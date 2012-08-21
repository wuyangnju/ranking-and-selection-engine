package hk.ust.felab.rase.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
@Configuration
public class GsonUtil {
	public static final String GSON_DES = "gsonDes";
	public static final String GSON_FLOAT = "gsonFloat";

	@Bean(name = GSON_DES)
	public static Gson gsonDes() {
		return new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	}

	@Bean(name = GSON_FLOAT)
	public static Gson gsonFloat() {
		return new GsonBuilder().serializeSpecialFloatingPointValues().create();
	}

}
