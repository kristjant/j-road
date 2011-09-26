/**
 * Copyright 2009 Webmedia Group Ltd. Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 **/

package ee.webmedia.xtee.endpoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ee.webmedia.xtee.model.BeanXTeeMessage;
import ee.webmedia.xtee.model.XTeeAttachment;
import ee.webmedia.xtee.model.XTeeHeader;
import ee.webmedia.xtee.model.XTeeMessage;
import ee.webmedia.xtee.util.SOAPUtil;
import ee.webmedia.xtee.util.XTeeUtil;

/**
 * Base class for X-Tee Spring web-service endpoints, extension classes must implement
 * {@link AbstractXTeeBaseEndpoint#invokeInternal(XTeeMessage request, XTeeMessage response)}.
 * 
 * @author Roman Tekhov
 * @author Dmitri Danilkin
 */
public abstract class AbstractXTeeBaseEndpoint implements MessageEndpoint {
  public final static String RESPONSE_SUFFIX = "Response";
  private boolean metaService = false;

  public final void invoke(MessageContext messageContext) throws Exception {
    SOAPMessage paringMessage = SOAPUtil.extractSoapMessage(messageContext.getRequest());
    SOAPMessage responseMessage = SOAPUtil.extractSoapMessage(messageContext.getResponse());

    // meta-service does not need 'header' element
    if (metaService) {
      responseMessage.getSOAPHeader().detachNode();
    }

    XTeeHeader pais = metaService ? null : parseXteeHeader(paringMessage);
    Document paring = metaService ? null : parseQuery(paringMessage);

    getResponse(pais, paring, responseMessage, paringMessage);
  }

  @SuppressWarnings("unchecked")
  private XTeeHeader parseXteeHeader(SOAPMessage paringMessage) throws SOAPException {
    XTeeHeader pais = new XTeeHeader();
    SOAPHeader header = paringMessage.getSOAPHeader();
    for (Iterator<Node> headerElemendid = header.getChildElements(); headerElemendid.hasNext();) {
      Node headerElement = headerElemendid.next();
      if (!SOAPUtil.isTextNode(headerElement) && headerElement.getFirstChild() != null) {
        pais.addElement(new QName(headerElement.getNamespaceURI(), headerElement.getLocalName()),
                        headerElement.getFirstChild().getNodeValue());
      }
    }
    return pais;
  }

  protected Document parseQuery(SOAPMessage queryMsg) throws Exception {
    Node bodyNode = SOAPUtil.getNodeByXPath(queryMsg.getSOAPBody().getFirstChild(), "//keha");

    if (bodyNode == null) {
      throw new IllegalStateException("Service is not metaservice, but query is missing mandatory body ('//keha\')");
    }
    Document query = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    bodyNode = query.importNode(bodyNode, true);
    query.appendChild(bodyNode);
    return query;
  }

  @SuppressWarnings("unchecked")
  protected void getResponse(XTeeHeader header, Document query, SOAPMessage responseMessage, SOAPMessage requestMessage)
      throws Exception {

    // Build request message
    List<XTeeAttachment> attachments = new ArrayList<XTeeAttachment>();
    for (Iterator<AttachmentPart> i = requestMessage.getAttachments(); i.hasNext();) {
      AttachmentPart a = i.next();
      attachments.add(new XTeeAttachment(a.getContentId(), a.getContentType(), a.getRawContentBytes()));
    }
    XTeeMessage<Document> request = new BeanXTeeMessage<Document>(header, query, attachments);

    // Build response message
    SOAPElement teenusElement = createXteeMessageStructure(requestMessage, responseMessage);
    if (!metaService)
      copyParing(query, teenusElement);
    Element kehaNode = teenusElement.addChildElement("keha");
    XTeeMessage<Element> response = new BeanXTeeMessage<Element>(header, kehaNode, new ArrayList<XTeeAttachment>());

    // Run logic
    invokeInternalEx(request, response, requestMessage, responseMessage);

    // Add any attachments
    for (XTeeAttachment a : response.getAttachments()) {
      AttachmentPart attachment = responseMessage.createAttachmentPart(a.getDataHandler());
      attachment.setContentId("<" + a.getCid() + ">");
      responseMessage.addAttachmentPart(attachment);
    }

    if (!metaService) {
      addHeader(header, responseMessage);
    }
  }

  protected SOAPElement createXteeMessageStructure(SOAPMessage requestMessage, SOAPMessage responseMessage)
      throws Exception {
    SOAPUtil.addBaseMimeHeaders(responseMessage);
    SOAPUtil.addBaseNamespaces(responseMessage);
    responseMessage.getSOAPPart().getEnvelope().setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");

    Node teenusElement = SOAPUtil.getFirstNonTextChild(requestMessage.getSOAPBody());

    if (teenusElement.getPrefix() == null || teenusElement.getNamespaceURI() == null) {
      throw new IllegalStateException("Service request is missing namespace.");
    }
    SOAPUtil.addNamespace(responseMessage, teenusElement.getPrefix(), teenusElement.getNamespaceURI());
    return responseMessage.getSOAPBody().addChildElement(teenusElement.getLocalName() + RESPONSE_SUFFIX,
                                                         teenusElement.getPrefix(),
                                                         teenusElement.getNamespaceURI());
  }

  private void copyParing(Document paring, Node response) throws Exception {
    Node paringElement = response.appendChild(response.getOwnerDocument().createElement("paring"));
    Node kehaNode = response.getOwnerDocument().importNode(paring.getDocumentElement(), true);

    NamedNodeMap attrs = kehaNode.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      paringElement.getAttributes().setNamedItem(attrs.item(i).cloneNode(true));
    }

    while (kehaNode.hasChildNodes()) {
      paringElement.appendChild(kehaNode.getFirstChild());
    }
  }

  protected void addHeader(XTeeHeader pais, SOAPMessage message) throws SOAPException {
    XTeeUtil.addXteeNamespace(message);
    for (QName qname : pais.getElemendid().keySet()) {
      if (qname.getNamespaceURI().equals(XTeeUtil.XTEE_NS_URI)) {
        XTeeUtil.addHeaderElement(message.getSOAPHeader(), qname.getLocalPart(), pais.getElemendid().get(qname));
      }
    }
  }

  /**
   * If true, request will be processed as a meta-request (an example of a meta-query is <code>listMethods</code>).
   */
  public void setMetaService(boolean metaService) {
    this.metaService = metaService;
  }

  /** Returns <code>true</code>, if this is a meta service. */
  public boolean isMetaService() {
    return metaService;
  }

  /**
   * This method can be overridden if you need direct access to the request and response messages.
   * 
   * @param request
   * @param response
   * @param responseMessage
   * @param requestMessage
   * @throws Exception
   */
  protected void invokeInternalEx(XTeeMessage<Document> request,
                                  XTeeMessage<Element> response,
                                  SOAPMessage responseMessage,
                                  SOAPMessage requestMessage) throws Exception {
    invokeInternal(request, response);
  }

  /**
   * Method which must implement the service logic, receives <code>request</code> and <code>response<code>.
   * 
   * @param request
   * @param response
   */
  protected void invokeInternal(XTeeMessage<Document> request, XTeeMessage<Element> response) throws Exception {
    throw new IllegalStateException("You must override either the 'invokeInternal' or the 'invokeInternalEx' method!");
  };
}