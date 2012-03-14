package hk.ust.felab.rase.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
@Configuration
public class GsonConf {

	@Bean(name = "gson")
	public Gson gson() {
		return new GsonBuilder().serializeNulls().create();
	}

}
