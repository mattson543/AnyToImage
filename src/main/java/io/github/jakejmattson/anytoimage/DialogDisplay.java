package io.github.jakejmattson.anytoimage;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.*;

class DialogDisplay
{
	static boolean isGraphical = true;
	static boolean shouldPrint = true;

	static void displayInfo(String title, String message)
	{
		if (shouldPrint)
			System.out.println(message);

		if (!isGraphical)
			return;

		Alert dialog = createDialog(Alert.AlertType.INFORMATION, title, message);
		dialog.showAndWait();
	}

	static void displayError(String title, String message)
	{
		if (shouldPrint)
			System.out.println(message);

		if (!isGraphical)
			return;

		Alert dialog = createDialog(Alert.AlertType.ERROR, title, message);
		dialog.showAndWait();
	}

	static void displayException(Exception e, String message)
	{
		if (shouldPrint)
		{
			System.out.println(message);
			e.printStackTrace();
		}

		if (!isGraphical)
			return;

		//Create dialog
		Alert alert = createDialog(Alert.AlertType.ERROR, "Something went wrong!", message);

		//Get exception as string
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionText = sw.toString();

		//Create a text area to display the exception
		TextArea textArea = new TextArea(exceptionText);
		textArea.setStyle("-fx-text-inner-color: red;");
		textArea.setEditable(false);

		//Format content
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		GridPane expContent = new GridPane();
		expContent.add(new Label("Exception stacktrace:"), 0, 0);
		expContent.add(textArea, 0, 1);

		//Display exception
		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
	}

	private static Alert createDialog(Alert.AlertType type, String title, String message)
	{
		Alert dialog = new Alert(type, message);
		dialog.setTitle(title);
		dialog.setHeaderText(null);

		return dialog;
	}
}