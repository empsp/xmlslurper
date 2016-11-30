Java XMLSlurper
===============

An attempt to port parsing capabilities offered by Groovy XMLSlurper into Java world. The following is not planned to be accurate projection, instead the most useful functions will be implemented.

## Overview

The basic premise is to facilitate xml file parsing by combining XPath/GPath similar data search with event based stream processing. XMLSlurper utilizes SAX/StAX parsers to perform bulk of the operations. Therefore one must expect events to occur in sequential order. Data retrieved is wrapped into org.xs4j.XMLNode and exposed via org.xs4j.listener.NodeListener functional interface (can be used in Java 1.8 Lambda expressions). The library has been compiled against Java 1.6 and therefore can be used in legacy code, as well.

## Download

### Maven (soon)

In order to use the library in your maven project, just declare the dependency inside your pom.xml file:

```maven
<dependency>
	<groupId>org.xs4j</groupId>
	<artifactId>xmlslurper</artifactId>
	<version>1.8.0</version>
</dependency>
```

### Gradle (soon)

In order to use the library in your gradle project, just declare the dependency inside your build.gradle file:

```gradle
dependencies {
    compile 'org.xs4j:xmlslurper:1.8.0'
}
```

## Usage

The usage is similar to SAX/Groovy XMLSlurper. A factory org.xs4j.XMLSlurperFactory provides org.xs4j.XMLSlurper implementation. The interface exposes two methods:
- org.xs4j.path.SlurpNode getNodes() - one can attach multiple listeners to nodes/attributes/values of choice (or to the whole document tree)
- void parse(InputStream inputStream) - triggers the document processing as in SAX/Groovy XMLSlurper

### Movie Database sample xml file

```xml
<?xml version="1.0" encoding="ISO-8859-1"?>
<MovieDb>
	<Movie title="Titanic" director="James Cameron">
		<Cast>
			<LeadActor>Leonardo DiCaprio</LeadActor>
			<LeadActress>Kate Winslet</LeadActress>
		</Cast>
	</Movie>
	<fr:Franchise fr:title="The Fast and the Furious" xmlns:fr="http://franchise">
		<Movie title="Fast and Furious 6" director="Justin Lin">
			<Cast>
				<LeadActor>Vin Diesel</LeadActor>
				<LeadActress>Michelle Rodriguez</LeadActress>
			</Cast>
		</Movie>
		<Movie title="Furious 7" director="James Wan">
			<Cast>
				<LeadActor>Vin Diesel</LeadActor>
				<LeadActress>Michelle Rodriguez</LeadActress>
			</Cast>
		</Movie>
		<Studios>
			<Studio>Universal Pictures</Studio>
		</Studios>
	</fr:Franchise>
	<Movie title="Forest Gump" director="Robert Zemeckis">
		<Cast>
			<LeadActor>Tom Hanks</LeadActor>
			<LeadActress>Robin Wright</LeadActress>
		</Cast>
		<Studios>
			<Studio>Paramount Pictures</Studio>
		</Studios>
		<Budget>55000000</Budget>
	</Movie>
</MovieDb>
```

### Scenarios

1. Simple 'take all' approach
	
	```java
	XmlSlurper xmlSlurper = XmlSlurperFactory.getInstance().getParser();
	xmlSlurper.getNodes().findAll((parent, node) -> {
		// your code here
		});
	xmlSlurper.parse("samplefile.xml");
	```
	
	In the above case, all available tags are going to be parsed and exposed to the developer. XMLSlurper provides SlurpNode interface via getNodes() method. SlurpNode can be further filtered to provide a more fine grained information. Here findAll() method is called with start/end node listener anonymous class/lambda expression used. The following table presents a list of first few events triggered in order.
	
	Event Index | Data available
	--- | ---
	1 | parent=null,<br />node=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='null', attrByQName={}}
	Comment | The first event is a start node event on a root node. In such a case, parent will have a null value.
	2 | parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={}},<br />node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={title=Titanic, director=James Cameron}}
	Comment | 'MovieDb' parent node's text has been updated with new line and spaces (here symbolized by tab '\t' character) that there are before the start of a 'Movie' node.
	3 | parent=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={title=Titanic, director=James Cameron}},<br />node=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='null', attrByQName={}}
	Comment | 'Movie' parent node's text has been updated with white characters as well.
	4 | parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}},<br />node=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='null', attrByQName={}}
	Comment | As a start node event, the text value of a 'LeadActor' node is still null.
	5 | parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}},<br />node=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='Leonardo DiCaprio', attrByQName={}}
	Comment | Here, at end node event, the text value of the 'LeadActor' node is finally available.
	6 | parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}},<br />node=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='null', attrByQName={}}
	Comment | Another start node event, and the text value of a 'LeadActress' node is null, however the text value of the 'Cast' parent node has been updated with additional white characters.
	7 | parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}},<br />node=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='Kate Winslet', attrByQName={}}
	8 | parent=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={title=Titanic, director=James Cameron}},<br />node=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t\n\t\t', attrByQName={}}
	9 | parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={}},<br />node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t', attrByQName={title=Titanic, director=James Cameron}}
	10 | parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t', attrByQName={}},<br />node=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='null', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}
	Comment | 'Franchise' node is from a separate namespace. Namespace and prefix fields will reflect that fact. Attributes hold additional metadata attribute 'xmlns:fr' and it's available together with ordinary attributes. Attribute 'title' has a prefix 'fr' which distinguishes it from the other nodes' 'title' attributes.
	.. | | 
	46 | parent=null,<br />node=XMLNodeImpl{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t\n', attributeByQName={}} |
