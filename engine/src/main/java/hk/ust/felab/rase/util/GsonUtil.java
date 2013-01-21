package hk.ust.felab.rase.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
	public static final String GSON_BRIEF = "gsonBrief";
	public static final String GSON_DES = "gsonDes";
	public static final String GSON_FLOAT = "gsonFloat";

	public static Gson gsonBrief() {
		return new GsonBuilder().serializeNulls().create();
	}

	public static Gson gsonDes() {
		return new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	}

	public static Gson gsonFloat() {
		return new GsonBuilder().serializeSpecialFloatingPointValues().create();
	}

}
