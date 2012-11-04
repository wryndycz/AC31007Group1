package com.example.stocktest2;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.sax.Element;
import android.util.Log;
import android.util.Xml;


//Singleton boundary class representing the remote Yahoo! Finance API
//Requests for stock data are routed through this class, parsed and returned in order
//to create ShareSet objects for each Share in a portfolio
public class YahooFinanceAPI 
{

	private static YahooFinanceAPI instance;
	
	
	private YahooFinanceAPI() 
	{
		// TODO Auto-generated constructor stub
	}
	
	public static YahooFinanceAPI getInstance()
	{
		
		if (instance == null)
			return new YahooFinanceAPI();
		else
			return instance;
	}
	
	public String[] fetchAndParse(String companyTicker)
	{
		URL url;	//URL object to access Yahoo! Finance
		try 
		{
			//s = Stock Symbol (+ ".L" = LON), l1 = Last Trade Price.
			url = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + companyTicker + ".L&f=st1l1"); //object.getTicker()
			
			//connect to Yahoo! finance.
			URLConnection urlConnection = url.openConnection();
			
			//Buffer the CVS file return from Yahoo!
			BufferedInputStream csvBuffer = new BufferedInputStream(urlConnection.getInputStream());			
			ByteArrayBuffer byteArray = new ByteArrayBuffer(50);

			//Append to byteArray until there is no more data (-1)
			int current = 0;
			while( (current = csvBuffer.read()) != -1)
			{
				byteArray.append((byte) current);
			}

			//Stores CVS into an unparsed string
			String stockCSV = new String(byteArray.toByteArray());
			
			//Split unparsed string into tokens at commas
			String[] rawTokens = (stockCSV.split(","));
			
			//Tidy up corresponding fields
			String stockSymbol = rawTokens[0].substring(1, rawTokens[0].length() - 1);	//Stock symbol (1st element) with removed "" quotation marks
			String stockTime = rawTokens[1].substring(1, rawTokens[1].length() - 3);	//Stock Time (2nd element) with removed "" quotation marks and 'Periods' (am/pm)
			String stockPrice = String.valueOf(Double.parseDouble(rawTokens[2]) / 100);	//Divide by 100 to get in pounds �
			
			//Compensate stock time for US time-zone
			String[] sub = stockTime.split(":");
			int first = Integer.parseInt(sub[0]) + 5;
			String compensatedStockTime = Integer.toString(first) + ":" + sub[1];
			
			//Return tidied up token array (separated CVS fields)
			return (new String[] {stockSymbol, compensatedStockTime, stockPrice});
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		//Exception will have been called at this point
		//Return null, so client can take appropriate action
		return null;
	}
	
	public boolean fetchAndParseShare(ShareSet object)
	{
		URL url;	//URL object to access Yahoo! Finance
		try 
		{
			/* 
			 * t1 = Last Trade Time
			 * l1 = Last Trade Price
			 * g = Daily High
			 * h = Daily Low
			 * v = Current Volume
			 * p = Previous Close Price
			 * p2 = Change from Previous Close in Percent
			 */
			url = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + object.getTicker() + ".L&f=t1l1ghvpp2"); 
			
			//connect to Yahoo! finance.
			URLConnection urlConnection = url.openConnection();
			
			//Buffer the CSV file return from Yahoo!
			BufferedInputStream csvBuffer = new BufferedInputStream(urlConnection.getInputStream());			
			ByteArrayBuffer byteArray = new ByteArrayBuffer(100);

			//Append to byteArray until there is no more data (-1)
			int current = 0;
			while( (current = csvBuffer.read()) != -1)
			{
				byteArray.append((byte) current);
			}

			//Stores CVS into an unparsed string
			String stockCSV = new String(byteArray.toByteArray());
			
			//Split unparsed string into tokens at commas
			String[] rawTokens = (stockCSV.split(","));
			
			//Tidy up corresponding fields
			String stockTime = timeParsing(rawTokens[0].substring(1, rawTokens[0].length() - 3));	//Stock trade time (1st element) with removed "" quotation marks and 'Periods' (am/pm)
			Double stockPrice = Double.parseDouble(rawTokens[1]) / 100;	//Divide by 100 to get in pounds �
			Double dailyHigh = Double.parseDouble(rawTokens[2]) / 100;  // Parse the daily high as Double
			Double dailyLow = Double.parseDouble(rawTokens[3]) / 100;  // Parse the daily low as Double
			Long volume = Long.parseLong(rawTokens[4]);
			Double prevPrice = Double.parseDouble(rawTokens[5]) / 100;
			String change = rawTokens[6].substring(1, rawTokens[6].length() - 1);
			
			object.setShareSet(stockTime, stockPrice, dailyHigh, dailyLow, volume, prevPrice, change);
			
			//Return true flag for proper execution
			return true;
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		//Exception will have been called at this point
		//Return null, so client can take appropriate action
		return false;
	}
	
	public String timeParsing(String theTime)
	{
		//Compensate stock time for US time-zone
		String[] sub = theTime.split(":");
		int first = Integer.parseInt(sub[0]) + 5;
		return (Integer.toString(first) + ":" + sub[1]);
	}

	public String getLastFriday()
	{
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        cal.set(Calendar.DAY_OF_WEEK, cal.FRIDAY);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        return  sdf.format(cal.getTime());    
    }
	
	/****************************
	 * Method to get the historical data using ichart URL - returns the content of the ichart query ,
	 * thus the data needed in a string, which could be parsed
	 * 
	 * e.g. ichart query: 
	 * http://ichart.finance.yahoo.com/table.csv?s=TSCO.L&a=09&b=26&c=2012&d=09&e=26&f=2012&g=d&ignore=.csv
	 * 
	 * Check Out This here:
	 * http://code.google.com/p/yahoo-finance-managed/wiki/csvHistQuotesDownload
	 * 
	 * @param companyTicker
	 * @return
	 */
	public String fetchAndParseHistory(String companyTicker)
	{
		try 
		{
			String[] dateTokens = (getLastFriday().split("-"));
			int month = ( Integer.parseInt(dateTokens[1]) - 1);
			
			String url_text ="http://ichart.finance.yahoo.com/table.csv?s=" + companyTicker + 
					".L&a="+ month +"&b="+dateTokens[0]+"&c="+dateTokens[2]+"&d="+ month +"&e="+dateTokens[0]+"&f="+dateTokens[2]+"&g=d&ignore=.csv"; 
			
			 HttpClient client = new DefaultHttpClient();
			 HttpGet request = new HttpGet(url_text);
             // Get the response
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String response_str = client.execute(request, responseHandler);
             
             return (url_text + "\n"+ response_str);
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		//Exception will have been called at this point
		//Return null, so client can take appropriate action
		return null;
	}
	
	
	/********************
	 * Method to get the historical data using YQL - returning a string representing the content of the 
	 * XML or JSON file returned from the query.
	 * 
	 * URL got from here:
	 * http://developer.yahoo.com/yql/console/?q=SELECT+%2A+FROM+yahoo.finance.quotes+WHERE+symbol%3D%22RBS.L%22%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env#h=SELECT%20*%20FROM%20yahoo.finance.historicaldata%20WHERE%20startDate%3D%222012-10-26%22%20AND%20symbol%3D%22TSCO.L%22%20AND%20endDate%3D%222012-10-26%22%0A%09%09
	 * 
	 * 
	 * @param companyTicker
	 * @return
	 */
	public String fetchAndParseYQLHistory(String companyTicker)
	{
		try 
		{
			 /***********************************
			  * XML file return using YQL query in URL
			  */
			 String date = getLastFriday();
			
			 String url_text = "http://query.yahooapis.com/v1/public/yql?q=SELECT%20*%20FROM%20yahoo.finance.historicaldata%20WHERE%20startDate%3D%22"+
			 date +"%22%20AND%20symbol%3D%22"+ companyTicker +".L%22%20AND%20endDate%3D%22"+
			 date +"%22%0A%09%09&diagnostics=true&env=http%3A%2F%2Fdatatables.org%2Falltables.env";
			 
			 HttpClient client = new DefaultHttpClient();
			 HttpGet request = new HttpGet(url_text);
             // Get the response
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String content = client.execute(request,responseHandler);

			 
             /* **********************************************************************
			  * XML file return - Try to parse with NameValuePairs
			  * 
	  		 String high = "";
             String low = "";
             String close = "";
             String volume = "";
             
				// Add post data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("High", high));
				nameValuePairs.add(new BasicNameValuePair("Low", low));
				nameValuePairs.add(new BasicNameValuePair("Close", close));
				nameValuePairs.add(new BasicNameValuePair("Volume", volume));
				
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				// Execute HTTP Post Request
				content = client.execute(request, responseHandler);	
			 
				******************************************************************/
			 
             
             
             
			 /**************************************************************************
			  * JSON file returned from YQL query in URL
			  * 
             String url = "http://query.yahooapis.com/v1/public/yql?q=SELECT%20*%20FROM%20yahoo.finance.historicaldata%20WHERE%20startDate%3D%22"+date
            		 +"%22%20AND%20symbol%3D%22"+companyTicker+".L%22%20AND%20endDate%3D%22"+date
            		 +"%22%0A%09%09&format=json&diagnostics=true&env=http%3A%2F%2Fdatatables.org%2Falltables.env";

			 HttpClient client = new DefaultHttpClient();
			 HttpGet request = new HttpGet(url);
             // Get the response
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String content = client.execute(request,responseHandler);
             
             ************************************************************************************/
			 
             
             return (content);

		}
		catch (IOException e) 
		{
            e.printStackTrace();
        }
		
		//Exception will have been called at this point
		//Return null, so client can take appropriate action
		return null;
	}
}
