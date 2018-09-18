/*
 * The MIT License
 * Copyright © 2018 Jake Mattson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jakejmattson.anytoimage;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.*;

import java.io.*;
import java.util.*;

//TODO validate dropped file extensions
//TODO fix "add directory" (image -> files)

public class ConversionController extends Application
{
	@FXML private Button btnAddFile;
	@FXML private Button btnAddDirectory;
	@FXML private Button btnOutput;
	@FXML private Button btnRemove;
	@FXML private Button btnSubmit;
	@FXML private Button btnClear;
	@FXML private RadioButton radFiles;
	@FXML private RadioButton radImage;
	@FXML private Pane dndPane;
	@FXML private ListView<String> lstInputs;
	@FXML private TextField txtOutput;
	@FXML private Label lblDirectionArrow;

	private List<File> inputFiles = new ArrayList<>();
	private File outputFile;

	private static final FileChooser.ExtensionFilter pngFilter =
			new FileChooser.ExtensionFilter("*.png", "*.png", "*.PNG");

	public static void main(String[] args)
	{
		if (args.length >= 3)
		{
			DialogDisplay.isGraphical = false;

			int conversionType = Integer.parseInt(args[0]);
			List<File> input = new ArrayList<>();

			for (int i = 1; i < args.length - 1; i++)
				input.add(new File(args[i]));

			File output = new File(args[args.length - 1]);

			//Process input
			if (conversionType == 0)
				FileToImage.convert(input, output);
			else if (conversionType == 1)
				ImageToFile.convert(input, output);
			else
				DialogDisplay.displayException(new Exception(), "Unrecognized conversion type!");
		}
		else if (args.length == 0)
			launch(args);
		else
			DialogDisplay.displayException(new Exception(), "Insufficient arguments!");
	}

	@Override
	public void start(Stage primaryStage) throws IOException
	{
		Parent root = FXMLLoader.load(getClass().getResource("/ConversionView.fxml"));
		primaryStage.setTitle("AnyToImage");
		primaryStage.setScene(new Scene(root));
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	@FXML
	public void initialize()
	{
		lstInputs.setItems(FXCollections.observableArrayList());

		addEvents();
		updateState(true);
	}

	private void addEvents()
	{
		//IO buttons
		btnAddFile.setOnAction(event -> addFile());
		btnAddDirectory.setOnAction(event -> addDirectory());
		btnOutput.setOnAction(event -> setOutput());

		//Action buttons
		btnRemove.setOnAction(event -> removeSelection());
		btnSubmit.setOnAction(event -> convertInput());
		btnClear.setOnAction(event -> clearAll());

		//Conversion direction
		radFiles.setOnAction(event -> updateState(true));
		radImage.setOnAction(event -> updateState(false));

		//Prepare drag and drop pane to receive files
		createDragHandler();
	}

	private void addFile()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Add input file");

		if (radImage.isSelected())
			chooser.getExtensionFilters().add(pngFilter);

		File selection = chooser.showOpenDialog(null);

		if (selection != null)
		{
			inputFiles.add(selection);
			lstInputs.getItems().add(selection.getName());
		}
	}

	private void addDirectory()
	{
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Add input directory");
		File selection = chooser.showDialog(null);

		if (selection != null)
		{
			inputFiles.add(selection);
			lstInputs.getItems().add(selection.getName());
		}
	}

	private void setOutput()
	{
		File selection;

		if (radFiles.isSelected())
		{
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Create an output file");
			chooser.getExtensionFilters().add(pngFilter);
			selection = chooser.showSaveDialog(null);
		}
		else
		{
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select an output directory");
			selection = chooser.showDialog(null);
		}

		if (selection != null)
		{
			outputFile = selection;
			txtOutput.setText(outputFile.getAbsolutePath());
		}
	}

	private void removeSelection()
	{
		int index = lstInputs.getSelectionModel().getSelectedIndex();

		if (index == -1)
			return;

		inputFiles.remove(index);
		lstInputs.getItems().remove(index);
	}

	private void convertInput()
	{
		if (!validateConversion())
			return;

		String infoTitle = "Operation successful!";
		String errorTitle = "Operation failed!";

		if (radFiles.isSelected())
		{
			boolean wasSuccessful = FileToImage.convert(inputFiles, outputFile);

			if (wasSuccessful)
				DialogDisplay.displayInfo(infoTitle, "Image created from files.");
			else
				DialogDisplay.displayError(errorTitle, "Image not created due to errors.");
		}
		else
		{
			boolean wasSuccessful = ImageToFile.convert(inputFiles, outputFile);

			if (wasSuccessful)
				DialogDisplay.displayInfo(infoTitle, "Files extracted from image.");
			else
				DialogDisplay.displayError(errorTitle, "Unable to extract any files.");
		}
	}

	private boolean validateConversion()
	{
		String title = "Incomplete field";

		if (inputFiles.isEmpty())
		{
			DialogDisplay.displayError(title, "Please add input files to continue.");
			return false;
		}

		if (outputFile == null)
		{
			DialogDisplay.displayError(title, "Please specify the output to continue.");
			return false;
		}

		return true;
	}

	private void clearAll()
	{
		//Clear data
		inputFiles.clear();
		outputFile = null;

		//Clear GUI
		lstInputs.getItems().clear();
		txtOutput.clear();
	}

	private void updateState(boolean isFileConversion)
	{
		clearAll();

		btnSubmit.setText(isFileConversion ? "Create Image" : "Extract Files");
		lblDirectionArrow.setText(isFileConversion ? "  ->   " : "  <-   ");
	}

	private void createDragHandler()
	{
		dndPane.setOnDragOver(event ->
		{
			if (event.getDragboard().hasFiles())
				event.acceptTransferModes(TransferMode.COPY);

			event.consume();
		});

		dndPane.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean success = false;

			if (dragboard.hasFiles())
			{
				List<File> droppedFiles = dragboard.getFiles();

				for (File file : droppedFiles)
				{
					inputFiles.add(file);
					lstInputs.getItems().add(file.getName());
				}

				success = true;
			}

			event.setDropCompleted(success);
			event.consume();
		});

		dndPane.setOnDragEntered(event ->
		{
			String style = "-fx-border-style: dashed; -fx-border-color: ";
			style += event.getDragboard().hasFiles() ? "lime" : "red";
			dndPane.setStyle(style);

			event.consume();
		});

		dndPane.setOnDragExited(event ->
		{
			dndPane.setStyle("-fx-border-style: dashed; -fx-border-color: black");

			event.consume();
		});
	}
}