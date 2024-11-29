/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.requests;

public abstract class Prompts {
    public static final String NON_DIAGRAM_PROMPT_1 = "I will provide you with a software or system architecture. Provide a detailed description of the architecture, including information on components, interactions, and overall structure. The response must be in JSON format with three keys: \"validity\", \"description\", and \"scores\".\n" +
            "\n" +
            "- The \"validity\" key should be 0 if the input is not a valid architecture (leave the other keys empty in this case) or 1 if it is.\n" +
            "\n" +
            "- The \"description\" key should contain a detailed description of the architecture in valid HTML. Include information on all components, their interactions, and the overall structure. Provide recommendations to improve the architecture, such as reducing bad smells or enhancing general software qualities (ISO/IEC 25010). Use only HTML for formatting (e.g., <p>, <ul>, <li>, <strong>), no markdown syntax.\n" +
            "\n" +
            "- The \"scores\" key should contain a string sequence of comma-separated rating scores (in percentages with 3 digits) for the given architecture on the aspects provided below. For example, 080,090,085,095,... would mean that the architecture gets an 80% rating on the first aspect, 90% on the second, and so on.\n" +
            "\n" +
            "Example response format:\n" +
            "{\n" +
            "  \"validity\": 1,\n" +
            "  \"description\": \"<p>The system architecture includes...</p><ul><li>Component 1...</li><li>Component 2...</li></ul>\",\n" +
            "  \"scores\": \"080,090,085,095\"\n" +
            "}\n" +
            "\n" +
            "ONLY respond with the JSON object as shown above. Any additional text will cause parsing errors.\n" +
            "\n" +
            "The analysis aspects are: ";
    public static final String NON_DIAGRAM_PROMPT_2 = "I will provide you with a software or system architecture and an analysis of it. Based on the analysis, please create an improved version of the architecture. The response must be in JSON format with three keys: \"validity\", \"description\", and \"scores\".\n" +
            "\n" +
            "- The \"validity\" key should be 0 if the input is not a valid architecture (leave the other keys empty in this case) or 1 if it is.\n" +
            "\n" +
            "- The \"description\" key should contain a detailed description of the improved architecture in valid HTML. This should include information on all components, their interactions, and the overall structure of the improved system. Additionally, provide reasons for the improvements made and how they address issues or enhance the architecture according to general software qualities (ISO/IEC 25010). Use only HTML for formatting (e.g., <p>, <ul>, <li>, <strong>), no markdown syntax.\n" +
            "\n" +
            "- The \"scores\" key should contain a string sequence of comma-separated rating scores (in percentages with 3 digits) for the improved architecture on the aspects provided below. For example, 080,090,085,095,... would mean that the improved architecture gets an 80% rating on the first aspect, 90% on the second, and so on.\n" +
            "\n" +
            "Example response format:\n" +
            "{\n" +
            "  \"validity\": \"1\",\n" +
            "  \"description\": \"<p>The improved system architecture includes...</p><ul><li>Component 1...</li><li>Component 2...</li></ul>\",\n" +
            "  \"scores\": \"080,090,085,095\"\n" +
            "}\n" +
            "\n" +
            "ONLY respond with the JSON object as shown above. Any additional text will cause parsing errors." +
            "\n" +
            "The analysis aspects are: ";;
    public static final String DIAGRAM_PROMPT_1 = "You are an expert in software architecture and PlantUML. I will provide with a software architecture, and you will generate PlantUML scripts for different types of diagrams based on the architecture. The output should be in JSON format, with each diagram type as a key and the corresponding PlantUML script as the string value. Ensure that all special characters within the JSON string are properly escaped.\n" +
            "The types of diagrams needed are:\n" +
            "1. System Architecture Diagram\n" +
            "2. Component Diagram\n" +
            "3. Class Diagram\n" +
            "4. Object Diagram\n" +
            "5. State Diagram\n" +
            "6. Use-Case Diagram\n" +
            "7. Sequence Diagram\n" +
            "Please ensure the PlantUML scripts are correct and complete for each type of diagram. Start the script with @startuml and end it with @enduml\n" +
            "Example response format:\n" +
            "{\n" +
            "  \"SystemArchitectureDiagram\": \"<PlantUML script>\",\n" +
            "  \"ComponentDiagram\": \"<PlantUML script>\",\n" +
            "  \"ClassDiagram\": \"<PlantUML script>\",\n" +
            "  \"ObjectDiagram\": \"<PlantUML script>\",\n" +
            "  \"StateDiagram\": \"<PlantUML script>\",\n" +
            "  \"UseCaseDiagram\": \"<PlantUML script>\",\n" +
            "  \"SequenceDiagram\": \"<PlantUML script>\"\n" +
            "}";
    public static final String DIAGRAM_PROMPT_2 = "You are an expert in software architecture and PlantUML. I will provide with a software architecture, and you will generate PlantUML scripts for different types of improved diagrams based on the improvement suggestions given below. The output should be in JSON format, with each diagram type as a key and the corresponding PlantUML script as the value.\n" +
            "The types of diagrams needed are:\n" +
            "1. System Architecture Diagram\n" +
            "2. Component Diagram\n" +
            "3. Class Diagram\n" +
            "4. Object Diagram\n" +
            "5. State Diagram\n" +
            "6. Use-Case Diagram\n" +
            "7. Sequence Diagram\n" +
            "Please ensure the PlantUML scripts are correct and complete for each type of diagram. Start the script with @startuml and end it with @enduml\n" +
            "Example response format:\n" +
            "{\n" +
            "  \"SystemArchitectureDiagram\": \"<PlantUML script>\",\n" +
            "  \"ComponentDiagram\": \"<PlantUML script>\",\n" +
            "  \"ClassDiagram\": \"<PlantUML script>\",\n" +
            "  \"ObjectDiagram\": \"<PlantUML script>\",\n" +
            "  \"StateDiagram\": \"<PlantUML script>\",\n" +
            "  \"UseCaseDiagram\": \"<PlantUML script>\",\n" +
            "  \"SequenceDiagram\": \"<PlantUML script>\"\n" +
            "} Here are the improvement suggestions: ";

    public static final String SUM_PROMPT = "Rewrite the following text such that it can be better analysed by a large language model. Ensure that no necessary information is omitted. The goal is not to shorten the text but to provide a more concise version that doesn't contain details irrelevant to the topic of the text. Do not use any markdown syntax and do not use astrix for formatting. Here is the text: \n";
}