package org.jboss.as.quickstarts.kitchensink.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.as.quickstarts.kitchensink.member.model.Member;

public class MemberRestIT {

    private static final String BASE_URL = "http://localhost:8080/kitchensink/rest/members";
    private HttpClient client;
    private ObjectMapper mapper;

    @Before
    public void setup() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        mapper = new ObjectMapper();
    }

    @Test
    public void testListMembers() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        JsonNode members = mapper.readTree(response.body());
        assertTrue(members.isArray());
    }

    @Test
    public void testCreateMember() throws IOException, InterruptedException {
        Member member = new Member();
        member.setName("REST Test User");
        member.setEmail("rest" + System.currentTimeMillis() + "@test.com");
        member.setPhoneNumber("1234567890");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(member)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testCreateInvalidMember() throws IOException, InterruptedException {
        // Test with invalid email
        Member member = new Member();
        member.setName("Invalid User");
        member.setEmail("invalid-email");
        member.setPhoneNumber("1234567890");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(member)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

        JsonNode errorResponse = mapper.readTree(response.body());
        assertTrue(errorResponse.has("email"));
    }

    @Test
    public void testCreateDuplicateEmail() throws IOException, InterruptedException {
        String email = "duplicate" + System.currentTimeMillis() + "@test.com";

        // Create first member
        Member member1 = new Member();
        member1.setName("First User");
        member1.setEmail(email);
        member1.setPhoneNumber("1234567890");

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(member1)))
                .build();

        client.send(request1, HttpResponse.BodyHandlers.ofString());

        // Try to create second member with same email
        Member member2 = new Member();
        member2.setName("Second User");
        member2.setEmail(email);
        member2.setPhoneNumber("0987654321");

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(member2)))
                .build();

        HttpResponse<String> response = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(409, response.statusCode());

        JsonNode errorResponse = mapper.readTree(response.body());
        assertTrue(errorResponse.has("email"));
        assertEquals("Email taken", errorResponse.get("email").asText());
    }

    @Test
    public void testGetMemberById() throws IOException, InterruptedException {
        // First create a member
        Member member = new Member();
        member.setName("Get By ID Test");
        member.setEmail("getbyid" + System.currentTimeMillis() + "@test.com");
        member.setPhoneNumber("1234567890");

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(member)))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, createResponse.statusCode());

        // Get the member list to find the ID
        HttpRequest listRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> listResponse = client.send(listRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode members = mapper.readTree(listResponse.body());

        // Find the member we just created
        Long memberId = null;
        for (JsonNode memberNode : members) {
            if (memberNode.get("email").asText().equals(member.getEmail())) {
                memberId = memberNode.get("id").asLong();
                break;
            }
        }
        assertNotNull("Should find the created member's ID", memberId);

        // Get the member by ID
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + memberId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        JsonNode retrievedMember = mapper.readTree(getResponse.body());
        assertEquals(member.getName(), retrievedMember.get("name").asText());
        assertEquals(member.getEmail(), retrievedMember.get("email").asText());
        assertEquals(member.getPhoneNumber(), retrievedMember.get("phoneNumber").asText());
    }

    @Test
    public void testGetNonExistentMember() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/999999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}