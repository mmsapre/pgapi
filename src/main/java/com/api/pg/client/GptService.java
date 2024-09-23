import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class GptService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GptConfig gptConfig;  // Inject the GptConfig to get the CSV file paths

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/completions";
    private static final String OPENAI_MODEL = "text-davinci-003";
    private static final String OPENAI_API_KEY = "your-openai-api-key"; // Set your OpenAI API key here

    // Function to read queryText from the CSV file
    public String readQueryTextCsv() {
        StringBuilder queryText = new StringBuilder();

        // Get the Query Text CSV file path from the configuration
        String queryTextCsvFilePath = gptConfig.getQueryTextCsvfile();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(queryTextCsvFilePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // Read query text from the CSV file
            for (CSVRecord record : csvParser.getRecords()) {
                queryText.append(record.get("query_text")).append("\n");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading Query Text CSV file: " + e.getMessage(), e);
        }

        return queryText.toString();
    }

    // Function to read trusted context from the CSV file
    public String readTrustedContextCsv() {
        StringBuilder trustedContext = new StringBuilder();

        // Get the Trusted Context CSV file path from the configuration
        String trustedContextCsvFilePath = gptConfig.getTrustedContextCsvfile();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(trustedContextCsvFilePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // Read trusted context (account data, etc.) from the CSV file
            for (CSVRecord record : csvParser.getRecords()) {
                String account = record.get("account");
                String holderName = record.get("holder_name");
                String balance = record.get("balance");
                String currency = record.get("currency");

                // Build the trusted source data to be included in the prompt
                trustedContext.append(String.format("Account: %s, Holder: %s, Balance: %s, Currency: %s\n",
                        account, holderName, balance, currency));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading Trusted Context CSV file: " + e.getMessage(), e);
        }

        return trustedContext.toString();
    }

    // Function to construct the GPT prompt based on the query text and trusted context data
    public String constructGptPrompt(String queryText, String trustedContext) {
        // Construct the full prompt with the query text and trusted context
        StringBuilder prompt = new StringBuilder();
        prompt.append("Using the trusted account data below, identify custom entities (ACCOUNT_NUMBER, BIC, HOLDER_NAME, BALANCE, CURRENCY) in the following text:\n\n");
        prompt.append("Trusted Account Data:\n");
        prompt.append(trustedContext).append("\n");
        prompt.append("Text: ").append(queryText).append("\n");

        return prompt.toString();
    }

    // Function to send the constructed prompt to OpenAI's GPT API and get the response
    public String getGptResponse(String prompt) {
        try {
            // Create the headers for the API call
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + OPENAI_API_KEY);

            // Build the request body with the prompt
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", OPENAI_MODEL);
            requestBody.put("prompt", prompt);
            requestBody.put("max_tokens", 150);

            // Create the HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Call OpenAI's API using RestTemplate
            ResponseEntity<Map> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, entity, Map.class);

            // Extract the GPT response from the API call
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    return (String) choices.get(0).get("text");
                }
            }
            throw new RuntimeException("No response from GPT API");
        } catch (Exception e) {
            throw new RuntimeException("Error in calling GPT API: " + e.getMessage(), e);
        }
    }

    // Main method to handle the GPT flow
    public Map<String, Object> detectEntities() {
        try {
            // Read Query Text CSV
            String queryText = readQueryTextCsv();

            // Read Trusted Context CSV
            String trustedContext = readTrustedContextCsv();

            // Construct the GPT prompt
            String prompt = constructGptPrompt(queryText, trustedContext);

            // Get the GPT response
            String gptResponse = getGptResponse(prompt);

            // Prepare the response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("prompt", prompt);
            responseData.put("gpt_response", gptResponse);

            return responseData;
        } catch (Exception e) {
            throw new RuntimeException("Error in detectEntities: " + e.getMessage(), e);
        }
    }
}
