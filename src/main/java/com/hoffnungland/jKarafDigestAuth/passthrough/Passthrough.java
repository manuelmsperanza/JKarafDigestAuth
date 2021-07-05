package com.hoffnungland.jKarafDigestAuth.passthrough;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



@WebServlet(urlPatterns={"/passthrough/*"})
public class Passthrough extends HttpServlet {

	private static final long serialVersionUID = -3615520267479278705L;

	private static final Logger logger = LoggerFactory.getLogger(Passthrough.class);

	private static final String wsseNs = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private static final String wsuNs = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	private Properties props;
	private DocumentBuilder builder;
	private Transformer transformer;

	@Override
	public void init() throws ServletException {
		super.init();
		logger.trace("Called");

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			this.builder = factory.newDocumentBuilder();

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			this.transformer = transformerFactory.newTransformer();

			this.props = new Properties();
			//this.xmlExtractor = new XmlExtractor();
			this.props.load(this.getClass().getResourceAsStream("/users.properties"));
		} catch (IOException | ParserConfigurationException | TransformerConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		logger.trace("Done");
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	
		logger.trace("Called");

		try {

			StringBuffer content = new StringBuffer();
			try (BufferedReader in = req.getReader();) {
				String line = null;
				while((line = in.readLine()) != null) {
					content.append(line.trim());
				}
			}
			
			String contentToSend = content.toString();

			String userName = null;
			String password = null;
			String passwordType = null;
			String nonce = null;
			String nonceEncodingType = null;
			String createdDate = null;
			Node soapHeader = null;

			StringReader stringReader = new StringReader(contentToSend);
			Document doc = builder.parse(new InputSource(stringReader));
			Element root = doc.getDocumentElement();
			NodeList soapHeaderNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "Header");
			if(soapHeaderNodes.getLength() > 0) {

				soapHeader = soapHeaderNodes.item(0);

				NodeList soapHeaderChildren = soapHeader.getChildNodes();
				for(int soapHeaderChildrenIdx = 0; soapHeaderChildrenIdx < soapHeaderChildren.getLength(); soapHeaderChildrenIdx++ ) {
					Node soapHeaderChild = soapHeaderChildren.item(soapHeaderChildrenIdx);
					logger.debug(soapHeaderChild.getLocalName() + " " + soapHeaderChild.getNamespaceURI());
					if(wsseNs.equals(soapHeaderChild.getNamespaceURI()) && "Security".equals(soapHeaderChild.getLocalName())) {
						Node wsseSecurityNode = soapHeaderChild;

						NodeList wsseSecurityChildren = wsseSecurityNode.getChildNodes();
						for(int wsseSecurityChildrenIdx = 0; wsseSecurityChildrenIdx < wsseSecurityChildren.getLength(); wsseSecurityChildrenIdx++ ) {
							Node wsseSecurityChild = wsseSecurityChildren.item(wsseSecurityChildrenIdx);
							logger.debug(wsseSecurityChild.getLocalName() + " " + wsseSecurityChild.getNamespaceURI());
							if(wsseNs.equals(wsseSecurityChild.getNamespaceURI()) && "UsernameToken".equals(wsseSecurityChild.getLocalName())) {
								Node wsseUsernameTokenNode = wsseSecurityChild;

								NodeList wsseUsernameTokenChildren = wsseUsernameTokenNode.getChildNodes();
								for(int wsseUsernameTokenChildrenIdx = 0; wsseUsernameTokenChildrenIdx < wsseUsernameTokenChildren.getLength(); wsseUsernameTokenChildrenIdx++ ) {
									Node wsseUsernameTokenChild = wsseUsernameTokenChildren.item(wsseUsernameTokenChildrenIdx);
									logger.debug(wsseUsernameTokenChild.getLocalName() + " " + wsseUsernameTokenChild.getNamespaceURI());

									if(wsseNs.equals(wsseUsernameTokenChild.getNamespaceURI())) {

										switch(wsseUsernameTokenChild.getLocalName()) {
										case "Username":
											Node wsseUsername = wsseUsernameTokenChild;
											userName = wsseUsername.getTextContent();
											break;
										case "Password":
											Node wssePassword = wsseUsernameTokenChild;
											password = wssePassword.getTextContent();
											NamedNodeMap wssePasswordAttrs = wssePassword.getAttributes();
											Node wssePasswordType = wssePasswordAttrs.getNamedItem("Type");
											if(wssePasswordType != null) {
												passwordType = wssePasswordType.getNodeValue();
											}
											break;
										case "Nonce":
											Node wsseNonce = wsseUsernameTokenChild;
											nonce = wsseNonce.getTextContent();
											NamedNodeMap wsseNonceAttrs = wsseNonce.getAttributes();
											Node wsseNonceEncodingType = wsseNonceAttrs.getNamedItem("EncodingType");
											if(wsseNonceEncodingType != null) {
												nonceEncodingType = wsseNonceEncodingType.getNodeValue();
											}
											break;
										}

									} else if(wsuNs.equals(wsseUsernameTokenChild.getNamespaceURI()) && "Created".equals(wsseUsernameTokenChild.getLocalName())) {
										Node wsuCreated = wsseUsernameTokenChild;
										createdDate = wsuCreated.getTextContent();
									}	

								}
								
							}

						}

					}

				}


			}

			logger.debug(userName);

			if("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest".equals(passwordType)) {

				if(userName == null || "".equals(userName) || !this.props.containsKey(userName)) {
					throw new ServletException("Invalid authentication");
				}

				if(nonce != null && !"".equals(nonce)) {

					String userPasswd = this.props.getProperty(userName);

					String digestCheck = GeneratePasswordDigest.buildPasswordDigest(userPasswd, nonce, nonceEncodingType, createdDate);
					if(!digestCheck.equals(password)) {
						throw new ServletException("Authentication failed");
					} else {
						logger.info(userName + " authenticated");
					}
				}

			} else {
				throw new ServletException("Invalid authentication");
			}

			root.removeChild(soapHeader);

			DOMSource source = new DOMSource(doc);

			try(StringWriter writer = new StringWriter()){

				StreamResult result = new StreamResult(writer);
				transformer.transform(source, result);
				contentToSend = writer.toString();
			}


			String targetUrl = req.getRequestURL().toString().replace(req.getContextPath()+ req.getServletPath(), "/services");

			try(PrintWriter writer = resp.getWriter();){			
				writer.write(getResponseFromBackend(contentToSend, targetUrl, req));
			}

		} catch (TransformerException | SAXException | NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}


		logger.trace("Done");
	}


	private String getResponseFromBackend(String content, String addr, HttpServletRequest req) throws IOException {
		logger.trace("Called");
		String body=null;
		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {


			HttpRequestBase method = null;
			switch(req.getMethod()) {
			case "GET":
				method = new HttpGet(addr);
			case "POST":
				HttpPost postMethod = new HttpPost(addr);
				InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(content.getBytes()));
				postMethod.setEntity(reqEntity);
				method = postMethod;
			}

			try (final CloseableHttpResponse response = httpclient.execute(method)) {
				body = EntityUtils.toString(response.getEntity());
			}

		}

		logger.trace("Done");
		return body;
	}


}
