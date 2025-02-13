/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.examples.integrations.oci.genai;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
@Disabled
public class GenAiServiceTest {

    private static Http1Client client;

    protected GenAiServiceTest(Http1Client client) {
        GenAiServiceTest.client = client;
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        OciGenAiMain.routing(builder);
    }

    @Test
    public void testChat() throws Exception {
        String userMessage = "Which are the most used Large Language Models?";
        try (Http1ClientResponse response = client.get("/genai/chat").queryParam("userMessage", userMessage).request()) {
            assertThat(response.status(), is(Status.OK_200));
            assertThat(response.as(String.class), containsString("BERT"));
        }
    }

    @Test
    public void testGenerate() throws Exception {
        String generateText = "Generate a job description for a data visualization expert with the following three "
                + "qualifications only:\n1) At least 5 years of data visualization expert\n2) A great eye for details\n3) "
                + "Ability to create original visualizations";
        try (Http1ClientResponse response = client.get("/genai/chat").queryParam("userMessage", generateText).request()) {
            assertThat(response.status(), is(Status.OK_200));
            String responseText = response.as(String.class);
            assertThat(responseText, containsString("Job Title:"));
            assertThat(responseText, containsString("Responsibilities"));
        }
    }

    @Test
    public void testSummarize() throws Exception {
        String summarizeText = "Provide a short summary of the following blog post in a bulleted list:\nOracle's strategy is "
                + "built around the reality that enterprises work with AI through three different modalities: Infrastructure, "
                + "models and services, and within applications.\nFirst, we provide a robust infrastructure for training and "
                + "serving models at scale. Through our partnership with NVIDIA, we can give customers superclusters, which are"
                + " powered by the latest GPUs in the market connected together with an ultra-low-latency RDMA over converged "
                + "ethernet (RoCE) network. This solution provides a highly performant, cost-effective method for training "
                + "generative AI models at scale. Many AI startups like Adept and MosaicML are building their products directly"
                + " on OCI.\nSecond, we provide easy-to-use cloud services for developers and scientists to utilize in fully "
                + "managed implementations. We're enabling new generative AI services and business functions through our "
                + "partnership with Cohere, a leading generative AI company for enterprise-grade large language models (LLMs). "
                + "Through our partnership with Cohere, we’re building a new generative AI service. This upcoming AI service, "
                + "OCI Generative AI, enables OCI customers to add generative AI capabilities to their own applications and "
                + "workflows through simple APIs.\nThird, we embed generative models into the applications and workflows that "
                + "business users use every day. Oracle plans to embed generative AI from Cohere into its Fusion, NetSuite, and"
                + " our vertical software-as-a-service (SaaS) portfolio to create solutions that provide organizations with the"
                + " full power of generative AI immediately. Across industries, Oracle can provide native generative AI-based "
                + "features to help organizations automate key business functions, improve decision-making, and enhance "
                + "customer experiences. For example, in healthcare, Oracle Cerner manages billions of electronic health "
                + "records (EHR). Using anonymized data, Oracle can create generative models adapted to the healthcare domain, "
                + "such as automatically generating a patient discharge summary or a letter of authorization for medical "
                + "insurance.\nOracle's generative AI offerings span applications to infrastructure and provide the highest "
                + "levels of security, performance, efficiency, and value.";
        try (Http1ClientResponse response = client.get("/genai/chat").queryParam("userMessage", summarizeText).request()) {
            assertThat(response.status(), is(Status.OK_200));
            String responseText = response.as(String.class);
            assertThat(responseText, containsString("Oracle"));
            assertThat(responseText, containsString("bulleted"));
        }
    }

    @Test
    public void testEmbedText() throws Exception {
        try (Http1ClientResponse response = client.get("/genai/embedText").queryParam("embeddingInputs",
                                                                                 "In order to maintain our growth, we "
                                                                                         + "need to track our billings to ensure "
                                                                                         + "we are charging our "
                                                                                         + "customers enough to support our "
                                                                                         + "business.",
                                                                                 " We have a system in place to track our "
                                                                                         + "billings and ensure we are billing "
                                                                                         + "our customers "
                                                                                         + "accurately.",
                                                                                 " We have a dedicated billing team that is "
                                                                                         + "responsible for generating invoices"
                                                                                         + " and tracking "
                                                                                         + "payments.",
                                                                                 " Our billing system is integrated with our "
                                                                                         + "customer relationship management "
                                                                                         + "(CRM) system, which "
                                                                                         + "allows us to track our billings and"
                                                                                         + " customer interactions in one place.",
                                                                                 " We use a third-party billing service to help"
                                                                                         + " us manage our billings and ensure "
                                                                                         + "we are billing our"
                                                                                         + " customers correctly.",
                                                                                 " We are committed to providing our customers "
                                                                                         + "with accurate billings and clear "
                                                                                         + "explanations of our "
                                                                                         + "charges.",
                                                                                 " Timely and accurate billing is important to "
                                                                                         + "our customers, and we strive to "
                                                                                         + "provide them with the"
                                                                                         + " best possible service.",
                                                                                 " We are constantly looking for ways to "
                                                                                         + "improve our billing process and "
                                                                                         + "ensure we are billing our "
                                                                                         + "customers fairly.",
                                                                                 " We are committed to being transparent with "
                                                                                         + "our customers about our billing "
                                                                                         + "process and how we "
                                                                                         + "calculate our charges.",
                                                                                 " Billing can be a complex process, and we are"
                                                                                         + " here to help our customers "
                                                                                         + "understand their bills "
                                                                                         + "and answer any questions they may "
                                                                                         + "have.",
                                                                                 " We value our customers and want to ensure "
                                                                                         + "that they are happy with our "
                                                                                         + "billing process and the "
                                                                                         + "services we provide.").request()) {
            assertThat(response.status(), is(Status.OK_200));
            assertThat(response.as(String.class), containsString("embeddings=[["));
        }
    }
}
