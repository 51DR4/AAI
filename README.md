# ArchitectAI
#### Overview
ArchitectAI (AAI) is an Android Application for Exploring Software Architecture Sketches with LLMs. Our app leverages the power of the Large Language Model Gemini to analyze software architectures. By sending parallel requests to the LLM and storing the results in a database, it optimizes response speed and efficiency. The user-friendly interface provides multiple features that allow users to capture, upload, and analyze images or text transcriptions of software architectures.

## Features

  **Home Page**:

  Dark Mode
  Help Button: Access a guide to using the app.
  History: Save analysis results for future reference. 

  **Main Functionalities**:

  Capture Image: Use the CameraX feature to capture high-quality images of software architectures.
  Upload Image: Upload an image from your device.
  Upload Text Description / Audio Transcription: Submit a text-based input of a software architecture for analysis.
  
  **Image Handling in camera and preview page**:

  Zoom: users can zoom while taking a picture to avoid impractical parts.
  Edit: After capturing or uploading an image, users can crop the image and edit it.
  Swipe Page: swipe the page to add additional textual information about the architecture before submitting.
  
  **Analysis Feature page**:

  Request Validation: Ensures the submission is a valid software architecture.
  LLM Analysis: Submits the image and additional information to LLM Gemini for analysis, displaying the result in a formatted HTML view.
  Output Customization: Choose from various outputs like Component Diagram or State Diagram, and view the architecture visualized using PlantUML/C4 format.
  Code Access: View and copy the PlantUML code for the generated diagrams.
  Enhanced Architecture: Option to get an improved version of the architecture based on user-defined filters such as energy efficiency, sustainability, security, and maintainability.
  History: Allow the user to choose whether to save the result or discard it.
  
  **Additional Features**:

  Summary Button: Summarizes long text to improve readability.
  Help Section: A detailed guide with an explanatory video on how to use the application.
  Sharing and Saving: Export results as PDFs or save images for future use.
  
## Installation
1. **Download the Source Code**
2. **Configure API key** 
   - Get an API key for Gemini at https://aistudio.google.com/apikey
   - In your local properties file, type `apiKey = <your key>`

## Usage
1. **Start the App**
2. **Capture or Upload an Image/Text**
3. **Edit and Preview**:
Use the provided tools to zoom, crop, or add additional text before submitting.
4. **Analyze**:
Submit your image or text for analysis. Review the results and choose from various output options to visualize your architecture.
5. (optional) **Save and Share**:
Save your analysis to the history or export the results as a PDF for sharing.

## Contributing
We welcome all contributions and improvements ideas! If you have suggestions or improvements, please open an issue and discuss your ideas or fork the repository and submit a pull request.

## License
This project is licensed under the MIT License. See the LICENSE file for details.