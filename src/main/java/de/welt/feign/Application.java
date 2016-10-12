package de.welt.feign;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestLine;
import feign.auth.BasicAuthRequestInterceptor;
import feign.sax.SAXDecoder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Application {

    public static void main(String[] args) {

        final SAXDecoder decoder = SAXDecoder.builder()
            .registerContentHandler(ArticleIdHandler.class)
            .build();

        final RequestInterceptor basicAuth =
            new BasicAuthRequestInterceptor("user", "password");


        ImageArticle api = Feign.builder()
            .decoder(decoder)
            .requestInterceptor(basicAuth)
            .target(ImageArticle.class, "https://ssl.welt.de/webservice/escenic/content");

        final ImageArticleData imageArticleData = api.imageArticleData("150264323");

        System.out.println("Yo " + imageArticleData.articleID + "  " + imageArticleData.title);
    }


    interface ImageArticle {

        @RequestLine("GET /{id}")
        ImageArticleData imageArticleData(final String id);
    }

    static class ImageArticleData {
        public String articleID;
        public String title;

        public ImageArticleData() {
            System.out.println("create new article");
        }

        public String getArticleID() {
            return articleID;
        }

        public void setArticleID(final String articleID) {
            this.articleID = articleID;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }
    }

    static class ArticleIdHandler extends DefaultHandler
        implements SAXDecoder.ContentHandlerWithResult<ImageArticleData> {

        private StringBuilder currentText = new StringBuilder();

        private ImageArticleData imageArticleData = new ImageArticleData();

        @Override
        public ImageArticleData result() {
            return imageArticleData;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("vdf:field")) {
                System.out.println("ATTR:" + attributes.getValue("name"));
            }
        }

        @Override
        public void endElement(String uri, String name, String qName) {

            String debug = String.format("uri: {%s} name: {%s} qName: {%s} currentText: {%s}", uri, name, qName, currentText);
            System.out.println(debug);

            if (qName.equals("dcterms:identifier")) {
                this.imageArticleData.articleID = currentText.toString().trim();
            } else if (qName.equals("updated")) {
                this.imageArticleData.title = currentText.toString().trim();
            }
            currentText = new StringBuilder();
        }


        @Override
        public void characters(char ch[], int start, int length) {
            currentText.append(ch, start, length);
        }
    }

}
