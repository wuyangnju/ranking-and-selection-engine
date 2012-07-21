package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.common.Conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class AgentThread {
//	private double[][] requestTask() throws HttpException, IOException {
//		GetMethod method = new GetMethod(Conf.masterUrl + Conf.REQUEST_TASK
//				+ "?slaveId=" + slaveId);
//		int retCode = client.executeMethod(method);
//		BufferedReader br = new BufferedReader(new InputStreamReader(
//				method.getResponseBodyAsStream()));
//		String altSystemsJson = br.readLine();
//		log.debug("Slave " + slaveId + " requests task with return code "
//				+ retCode + " and result " + altSystemsJson);
//		return gson.fromJson(altSystemsJson, double[][].class);
//	}
//
//	private long submitSamples(double[][] samplesWithSysId)
//			throws HttpException, IOException {
//		PostMethod method = new PostMethod(Conf.masterUrl + Conf.SUBMIT_SAMPLE);
//		String samplesJson = gson.toJson(samplesWithSysId);
//		method.addParameter("samplesJson", samplesJson);
//		int retCode = client.executeMethod(method);
//		BufferedReader br = new BufferedReader(new InputStreamReader(
//				method.getResponseBodyAsStream()));
//		String submitRetCodeJson = br.readLine();
//		log.debug("Slave " + slaveId + " submit sample "
//				+ gson.toJson(samplesJson) + "with return code " + retCode
//				+ " and result " + submitRetCodeJson);
//		return Long.parseLong(submitRetCodeJson);
//	}
}
