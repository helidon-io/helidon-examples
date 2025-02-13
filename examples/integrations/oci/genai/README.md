# Helidon Generative AI SE Examples

This example demonstrates how user can easily interact with their GenAI service running in OCI.

It requires a running OCI GenAI service endpoint. See [OCI Documentation](https://docs.oracle.com/en-us/iaas/Content/generative-ai/overview.htm) for more details.

Before running the test, make sure to update required properties in `application.yaml`

- oci.config.profile: This is name of session-token profile that you create beforehand.
- oci.genai.region: This is the subscribed region e.g. us-chicago-1, where your GenAI service is running.
- oci.genai.compartment_id: This is the OCID of your compartment, where your GenAI service is running.
- oci.genai.chat.model_id: This is the OCID of LLM that you want to use.
- oci.genai.embedding.model_id: This is the OCID of Embedding model that you want to use.

Once you have updated required properties, you can run the example:

```shell
mvn package
java -jar ./target/helidon-examples-integrations-oci-genai.jar
```  

To verify that, you can retrieve wallet and do database operation:

```shell
curl curl http://localhost:8080/genai/chat?userMessage=Which%2Bare%2Bthe%2Bmost%2Bused%2BLarge%2BLanguage%2BModels%253F
```

You should see 

```shell
ChatResult(super=BmcModel(__explicitlySet__=[modelId, modelVersion, chatResponse])modelId=ocid1.generativeaimodel.oc1.us-chicago-1.amaaaaaask7dceyaiir6nnhmlgwvh37dr2mvragxzszqmz3hok52pcgmpqta, modelVersion=1.0.0, chatResponse=GenericChatResponse(super=BaseChatResponse(super=BmcModel(__explicitlySet__=[timeCreated, choices])), timeCreated=Thu Feb 13 12:52:17 CST 2025, choices=[ChatChoice(super=BmcModel(__explicitlySet__=[finishReason, index, message, logprobs])index=0, message=AssistantMessage(super=Message(super=BmcModel(__explicitlySet__=[content])content=[TextContent(super=ChatContent(super=BmcModel(__explicitlySet__=[text])), text=Here are some of the most widely used Large Language Models (LLMs) today:

1. **BERT (Bidirectional Encoder Representations from Transformers)**: Developed by Google in 2018, BERT is a multi-layer, bidirectional transformer encoder that has achieved state-of-the-art results in various NLP tasks, including language translation, question-answering, and sentiment analysis.

2. **RoBERTa (Robustly Optimized BERT Pretraining Approach)**: Released in 2019, RoBERTa is a variant of BERT that has been fine-tuned to achieve better performance in various NLP tasks. It's known for its effectiveness in tasks requiring long-range dependencies, such as question-answering.

3. **Transformers-XL**: Developed by Google in 2019, Transformers-XL is a neural network architecture designed to handle long-range dependencies. It's been applied in tasks such as text generation and machine translation.

4. **T5 (Text-to-Text Transfer Transformer)**: Introduced by Google in 2020, T5 is a unifying framework for NLP tasks, enabling users to convert all text-based tasks into a text-to-text format.

5. **LLaMA (Large Language Model Meta AI)**: Developed by Meta AI in 2022, LLaMA is a large language model designed for conversational AI applications. It can process up to 65,536 tokens and has achieved state-of-the-art results in various NLP tasks.

6. **PaLM (Pathways Language Model)**: Released by Google in 2022, PaLM is a large language model designed to handle the scale and complexity of modern NLP tasks. It has been fine-tuned for various NLP tasks and achieved state-of-the-art results.

7. **GPT (Generative Pre-trained Transformer)**: Developed by OpenAI in 2018, GPT is a large language model designed for text generation. Its architecture is based on the transformer model and has achieved state-of-the-art results in various NLP tasks, including language translation and text summarization. The GPT architecture has been used to develop several variants, such as GPT-2 (2019) and GPT-3 (2020).

8. **ChatGPT (Chat Generative Pre-trained Transformer)**: Developed by OpenAI in 2022, ChatGPT is an AI chatbot that uses a variant of the GPT-3 language model. It's designed for conversational applications and can process user input, generate human-like text responses, and even provide feedback.

These are just a few of the most popular large language models used today. The field of NLP is constantly evolving, and new LLMs are being developed to address specific challenges and applications.)]), name=null), finishReason=stop, logprobs=Logprobs(super=BmcModel(__explicitlySet__=[])textOffset=null, tokenLogprobs=null, tokens=null, topLogprobs=null))]))
```
