import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class GptService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/completions";
    private static final String OPENAI_MODEL = "text-davinci-003";
    private static final String OPENAI_API_KEY = "your-openai-api-key"; // Set your OpenAI API key here

    // Function to construct the GPT prompt based on the query text and trusted source
    public String constructGptPrompt(String queryText, Map<String, Object> trustedSource) {
        StringBuilder trustedContext = new StringBuilder();

        // Add trusted account data if available
        if (trustedSource != null && trustedSource.containsKey("accounts")) {
            List<Map<String, Object>> accounts = (List<Map<String, Object>>) trustedSource.get("accounts");
            for (Map<String, Object> record : accounts) {
                trustedContext.append(String.format("Account: %s, BIC: %s, Holder: %s, Balance: %s, Currency: %s\n",
                        record.get("account"),
                        record.get("bic"),
                        record.get("holder_name"),
                        record.get("balance"),
                        record.get("currency")));
            }
        }

        // Construct the full prompt with the query text and trusted source
        StringBuilder prompt = new StringBuilder();
        prompt.append("Using the trusted account data below, identify custom entities (ACCOUNT_NUMBER, BIC, HOLDER_NAME, BALANCE, CURRENCY) in the following text:\n\n");
        prompt.append("Trusted Account Data:\n");
        prompt.append(trustedContext.toString()).append("\n");
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
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                return (String) choices.get(0).get("text");
            } else {
                throw new RuntimeException("No response from GPT API");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in calling GPT API: " + e.getMessage(), e);
        }
    }

    // Main method to handle the GPT flow
    public Map<String, Object> detectEntities(String queryText, Map<String, Object> trustedSource) {
        try {
            // Construct the GPT prompt
            String prompt = constructGptPrompt(queryText, trustedSource);

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
