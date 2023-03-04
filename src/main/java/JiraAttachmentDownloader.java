import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Attachment;
//import com.atlassian.jira.rest.client.api.domain.BasicCredentials;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.atlassian.util.concurrent.Promise;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Properties;



public class JiraAttachmentDownloader {

    public static void main(String[] args) throws Exception {
        Properties property = new Properties();
        // Set up connection to Jira REST API using bearer token
        //URI jiraServerUri = new URI("https://peteridenu.atlassian.net//"); //insert pnc jira url here
        URI jiraServerUri = new URI(property.getProperty("https://peteridenu.atlassian.net"));
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        HttpClient httpClient = HttpClients.createDefault();
        //Header authHeader = new BasicHeader("Authorization", "Bearer " + "ATATT3xFfGF0MC9JchIiAbaE6vDvMu-E74F4N1LOs6jd_hhLtuzOTAf3XeMGEJ-DCZzK1AR8T6W3OIWHcZ0dsdjiSPkJbVfxCtN90y_1z8xe5mPj_4iptVaELF2KABJ6d2BzjZGC9LuFMIhMmmERtPP40AJbNanCNHuOPKlkK-uWssEjlcCOWh4=DB7F0F18");
        //JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "ATATT3xFfGF0MC9JchIiAbaE6vDvMu-E74F4N1LOs6jd_hhLtuzOTAf3XeMGEJ-DCZzK1AR8T6W3OIWHcZ0dsdjiSPkJbVfxCtN90y_1z8xe5mPj_4iptVaELF2KABJ6d2BzjZGC9LuFMIhMmmERtPP40AJbNanCNHuOPKlkK-uWssEjlcCOWh4=DB7F0F18", null);
        JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "peteridenu@gmail.com", "Nicaea10");

        // Define JQL query to select a range of set stories
        String jqlQuery = "project = SMSA AND issuetype = Story AND key >= SMSA-1 AND key <= SMSA-5";

        // Execute JQL query and retrieve list of issues (stories) that match the query
        Promise<SearchResult> searchResultPromise = restClient.getSearchClient().searchJql(jqlQuery);
        Iterable<Issue> issues = searchResultPromise.claim().getIssues();

        // Download attachments for each issue (story) into separate folders with the same name as the story
        for (Issue issue : issues) {
            List<Attachment> attachments = (List<Attachment>) restClient.getIssueClient().getAttachment(URI.create(issue.getKey())).claim();

            // Create a new folder with the same name as the current issue (story)
            String issueName = issue.getSummary().replaceAll("[^a-zA-Z0-9.-]", "_"); // remove special characters from story name
            File issueFolder = new File(issueName);
            if (!issueFolder.exists()) {
                issueFolder.mkdir();
            }

            // Download attachments for the current issue (story) into the new folder
            for (Attachment attachment : attachments) {
                HttpGet request = new HttpGet(attachment.getContentUri().toString());
                request.setHeader("Authorization", "Bearer " + "ATATT3xFfGF0MC9JchIiAbaE6vDvMu-E74F4N1LOs6jd_hhLtuzOTAf3XeMGEJ-DCZzK1AR8T6W3OIWHcZ0dsdjiSPkJbVfxCtN90y_1z8xe5mPj_4iptVaELF2KABJ6d2BzjZGC9LuFMIhMmmERtPP40AJbNanCNHuOPKlkK-uWssEjlcCOWh4=DB7F0F18");
                byte[] attachmentBytes = httpClient.execute(request, response -> {
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        return null;
                    }
                    return IOUtils.toByteArray(entity.getContent());
                });
                ContentType contentType = ContentType.parse(attachment.getMimeType());
                String attachmentFilename = attachment.getFilename();
                File attachmentFile = new File(issueFolder, attachmentFilename);
                FileUtils.writeByteArrayToFile(attachmentFile, attachmentBytes);
            }
        }
    }
}
