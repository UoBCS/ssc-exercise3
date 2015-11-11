# SSC Exercise 3
> SSC Exercise 3 - JavaMail API

## Client package structure

- communication
	- IMAPClient.java
	- SMTPClient.java
	- SimpleAuthenticator.java
- exceptions
	- ClientException.java
- gui
	- models
		- MessagesModel.java
	- views
		- CustomTextField.java
		- MessagesView.java
	- EmailClient.java
	- MessageBox.java
- utils
	- Utils.java

## GUI

Some parts of the GUI were built using WindowBuilder in Eclipse.

## Comments

All the code is commented using JavaDOC as well as internal comments in each method.

## Exception handling

All exceptions are propagated to the appropriate parts (i.e. the GUI component events).
Therefore all methods are "pure" in the sense that they just perform their task, delegating exception handling to the GUI.
