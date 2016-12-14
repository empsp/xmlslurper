XMLSlurper for Java
===============

An attempt to port parsing capabilities offered by Groovy XMLSlurper into the Java world. The following is not planned to be accurate projection, instead the most useful functions will be implemented.

## Overview

The basic idea is to facilitate reading of XML documents by combining XPath/GPath similar data search with event based text stream processing. XMLSlurper utilizes SAX parser to perform bulk of the operations. Events occurence is going to align with what SAX provides i.e. data consumed and exposed sequentially. XMLSlurper wraps acquired data into a convienent `XMLNode` data structure which is available through `NodeListener` functional interface (compatible with Java 1.8 Lambda expressions). The library has been compiled against Java 1.6 and can be used in legacy code, as well.

## Download

### Maven (soon)

In order to use the library in your maven project, just declare the dependency inside your pom.xml file:

```maven
<dependency>
	<groupId>org.xs4j</groupId>
	<artifactId>xmlslurper</artifactId>
	<version>2.0.0</version>
</dependency>
```

### Gradle (soon)

In order to use the library in your gradle project, just declare the dependency inside your build.gradle file:

```gradle
dependencies {
    compile 'org.xs4j:xmlslurper:2.0.0'
}
```

## Usage

### Terminology

- Element is a xml element that either begins with a start-tag and ends with a matching end-tag or consists only of an empty-element tag.
- Node is a Java data structure reflecting a particular xml element.
- Path is a location of the node within the document tree.

### Design

Contrary to the low level API SAX provides, XMLSlurper exposes start/end element tag related events. Upon reading XML document, each tag triggers `NodeListener.onNode()` method on a condition that current element matches the given path. The path is configured via `XMLSlurper.getNodes()` method. Every element of the path can have a `NodeListener` attached to, moreover start-tag/end-tag/or both can be selected if needed. Upon those events, `XMLNode` data structure is available, however with different information. Among element prefix, namespace, local name and attributes, end-tag event suplements `XMLNode` with text data.

### Capabilities

The following is a list of XMLSlurper capabilities:

1. Read all elements (with attributes and text) from the xml file.
2. Read all elements that match the given path.
3. Read all elements that match the given path and have the given attribute.
4. Read all elements that match the given path and have the attribute with specific value/values different than/value matching regex expression.
5. Read all elements that are children of the given element (wildcard `*`).
6. Read all elements that are descendants of the given element (wildcard `**`).
7. Read n-th/all n-th elements with respect to capabilities above.

All of the above will return nodes together with their parent nodes. This way, the developers have the possibility to deduce where the node is placed within the descendants tree of the XML document.

Additionally the library ensures that:

1. `@NotNull`/`@Nullable` interfaces will be adhered to.
2. XMLSlurper will release all of the resources after the execution (including the `InputStream` given).
3. Single XMLSlurper instance can be reused many times, however the paths must be redefined.
4. `XMLNode` objects are meaningfully equal only by the ids which will be unique only to the scope of a single xml file processing. Consecutive parsing of a different xml file will produce nodes with different data but matching ids. Therefore `XMLNode` objects are not fit and designed to be stored, instead the information should be extracted and the objects should be left for garbage collection.
5. Namespace awareness can be turned on/off (feature turned on by default).
6. Without schema validation, formatting information (indentations/carriage returns etc) will be available within text data.

#### Advanced search

##### Sibling search - wildcard \*

Any time it is required to retrieve all children of the given element, a wildcard (`*`) character can be used as a node name. Similary as in XPath, wildcards can be stacked to get all grandchildren (and so on..) of the given element.

##### Descendants search - double wildcard \*\*

It is also possible to retrieve all elements being descendants of the given element with double wildcard (`**`) character. Using double wildcard with additional node name at the end of the path eg. '\*\*.Foo' will return all nodes named 'Foo' found traversing path beginning from the root element (excluding root itself). Given the above example any 'Foo' element found underneath the parent 'Foo' element would be shown, as well.

### Examples

#### Movie Database sample xml file

```xml
<?xml version="1.0" encoding="UTF-8"?>
<MovieDb xmlns="http://movieDb">
	<Movie title="Titanic" director="James Cameron">
		<Cast>
			<LeadActor>Leonardo DiCaprio</LeadActor>
			<LeadActress>Kate Winslet</LeadActress>
		</Cast>
	</Movie>
	<fr:Franchise fr:title="Fast and Furious" xmlns:fr="http://franchise">
		<Movie title="The Fast and the Furious" director="Rob Cohen">
			<Cast>
				<LeadActor>Vin Diesel</LeadActor>
				<LeadActress>Michelle Rodriguez</LeadActress>
			</Cast>
		</Movie>
		<Movie title="2 Fast 2 Furious" director="John Singleton">
			<Cast>
				<LeadActor>Vin Diesel</LeadActor>
				<LeadActress>Eva Mendes</LeadActress>
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

#### Scenarios

1. Simple 'take all' approach
	
	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	xmlSlurper.getNodes().findAll((parent, node) -> {
		// your code here
	});
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	In the following scenario, all available elements are going to be parsed and exposed to the developer.
	
	The following table provides a list of first few events triggered in order:
	
	Event Id | Data available
	--- | ---
	1 | `parent=null, node=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='null', attrByQName={xmlns=http://movieDb}}`
	Comment | The first event is a start node event on a root node. In such a case, parent will have a `null` value.
	2 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={title=Titanic, director=James Cameron}}`
	Comment | 'MovieDb' parent node's text has been updated with new line and spaces (here symbolized by tab `\t` character) that there are before the start of a 'Movie' node. The information may be irrelevant and could be omitted, however without a schema `mixed` attribute on the element definition, XMLSlurper doesn't know how to handle whitespaces, therefore it's safe to assume that all characters count.
	3 | `parent=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={title=Titanic, director=James Cameron}}, node=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='null', attrByQName={}}`
	Comment | 'Movie' parent node's text has been updated with white characters as well.
	4 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}}, node=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='null', attrByQName={}}`
	Comment | As a start node event, the text value of a 'LeadActor' node is still `null`.
	5 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}}, node=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='Leonardo DiCaprio', attrByQName={}}`
	Comment | Here, at end node event, the text value of the 'LeadActor' node is finally available.
	6 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}}, node=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='null', attrByQName={}}`
	Comment | Another start node event, and the text value of a 'LeadActress' node is `null`, however the text value of the 'Cast' parent node has been updated with additional white characters found before processing 'LeadActress'.
	7 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}}, node=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='Kate Winslet', attrByQName={}}`
	8 | `parent=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={title=Titanic, director=James Cameron}}, node=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t\n\t\t', attrByQName={}}`
	9 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t', attrByQName={title=Titanic, director=James Cameron}}`
	10 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='null', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}`
	Comment | 'Franchise' node is from a separate namespace. Namespace and prefix fields will reflect that fact. Attributes hold additional metadata attribute 'xmlns:fr' and it's available together with ordinary attributes. Attribute 'title' has a prefix 'fr' which distinguishes it from the other nodes' 'title' attributes.
	.. | | 
	46 | `parent=null, node=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t\n', attributeByQName={}}` |

2. Read all 'Movie' nodes directly under 'MovieDb' root node

	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	xmlSlurper.getNodes().node("MovieDb").node("Movie").findAll((parent, node) -> {
		// your code here
	});
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	The following table provides a list of all triggered events in order:
	
	Event Id | Data available
	--- | ---
	1 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=James Cameron, title=Titanic}}`
	2 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t', attrByQName={director=James Cameron, title=Titanic}}`
	3 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`
	4 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t\t\n\t\t\n\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`

3. Read all nodes having 'title' attribute

	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	xmlSlurper.getNodes().attr("title").findAll((parent, node) -> {
		// your code here
	});
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	The following table provides a list of all triggered events in order:
	
	Event Id | Data available
	--- | ---
	1 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=James Cameron, title=Titanic}}`
	2 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t', attrByQName={director=James Cameron, title=Titanic}}`
	3 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Rob Cohen, title=The Fast and the Furious}}`
	Comment | 'Franchise' node is only available here as a parent node. It has not been found as a node because it has a 'title' attribute within different namespace.
	4 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attrByQName={director=Rob Cohen, title=The Fast and the Furious}}`
	5 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=John Singleton, title=2 Fast 2 Furious}}`
	6 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attrByQName={director=John Singleton, title=2 Fast 2 Furious}}`
	7 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`
	8 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t\t\n\t\t\n\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`

	
	However, searching for an attribute with prefix (qName) will have different results:
	
	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	xmlSlurper.getNodes().attr("fr:title").findAll((parent, node) -> {
		// your code here
	});
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	The following table provides a list of all triggered events in order:
	
	Event Id | Data available
	--- | ---
	1 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='null', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}`
	Comment | Here, 'Franchise' node is a result node having a parent node 'MovieDb'.
	2 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t\n\t\t\n\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}`

4. Read all nodes having 'title' attribute value containing word 'Furious' or equal to 'Forest Gump'
	
	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	
	NodeListener listener = (parent, node) -> {
		// your code here
	};
	
	SlurpAttribute attr = xmlSlurper.getNodes().attr("title");
	attr.regex(".*Furious.*").findAll(listener);
	attr.is("Forest Gump").findAll(listener);
	
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	Please note, `SlurpAttribute` extraction to a local variable is just for brevity, the same outcome can still be achieved by:
	
	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	
	NodeListener listener = (parent, node) -> {
		// your code here
	};
	
	xmlSlurper.getNodes().attr("title").regex(".*Furious.*").findAll(listener);
	xmlSlurper.getNodes().attr("title").is("Forest Gump").findAll(listener);
	
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	The following table provides a list of all triggered events in order:
	
	Event Id | Data available
	--- | ---
	1 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Rob Cohen, title=The Fast and the Furious}}`
	2 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attrByQName={director=Rob Cohen, title=The Fast and the Furious}}`
	3 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=John Singleton, title=2 Fast 2 Furious}}`
	4 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attrByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attrByQName={director=John Singleton, title=2 Fast 2 Furious}}`
	5 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`
	6 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={xmlns=http://movieDb}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t\t\n\t\t\n\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`


5. Read cast from movies (excluding franchises)
	
	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	xmlSlurper.getNodes("MovieDb", "Movie", "Cast", "*").findAll((parent, node) -> {
		// your code here
	});
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	For convenience method `getNodes("MovieDb", "Movie", "Cast", "*")` used which is equal to construct `getNodes().node("MovieDb").node("Movie").node("Cast").node("*")`.
	
	The following table provides a list of all triggered events in order:
	
	Event Id | Data available
	--- | ---
	1 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}}, node=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='null', attrByQName={}}`
	2 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}}, node=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='Leonardo DiCaprio', attrByQName={}}`
	3 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}}, node=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='null', attrByQName={}}`
	4 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}}, node=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='Kate Winslet', attrByQName={}}`
	5 | `parent=XMLNode{id=17, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}}, node=XMLNode{id=18, namespace='null', prefix='null', localName='LeadActor', text='null', attrByQName={}}`
	6 | `parent=XMLNode{id=17, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}}, node=XMLNode{id=18, namespace='null', prefix='null', localName='LeadActor', text='Tom Hanks', attrByQName={}}`
	7 | `parent=XMLNode{id=17, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}}, node=XMLNode{id=19, namespace='null', prefix='null', localName='LeadActress', text='null', attrByQName={}}`
	8 | `parent=XMLNode{id=17, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}}, node=XMLNode{id=19, namespace='null', prefix='null', localName='LeadActress', text='Robin Wright', attrByQName={}}`
	
	However, we've lost the information regarding which movie given cast belongs to. Also, some of the events are meaningless because text information is not yet available. Lets fix that:
	
	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	
	Map<XMLNode, XMLNode> movieByCast = new HashMap<>();
	NodeListener castListener = (cast, person) -> {
		XMLNode movie = movieByCast.get(cast);
		
		// your code here
	};
	
	SlurpNode cast = xmlSlurper.getNodes("MovieDb", "Movie", "Cast");
	cast.findAll((movie, cast) -> movieByCast.put(cast, movie), null);
	cast.node("*").findAll(null, castListener);
	
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	Even though `XMLNode` has id based equal/hashcode it's still perfectly eligible to be used in maps and sets for utility purposes. Since start node event of the 'MovieDb.Movie.Cast' node is sufficient to collect supporting data, end node event listener is provided as a `null` reference. To gather actor/actress names, text information needs to be read from immediate children (`*`) of the 'Cast' node, hence the use of end node listener and start node listener given as a `null` reference.
	Please note, for very large xml files, the above construct requires grooming of the `HashMap` to protect it from `StackOverflowError`.
	
	The following table provides a list of all triggered events on 'castListener' in order:
	
	Event Id | Data available
	--- | ---
	1 | `movie=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=James Cameron, title=Titanic}}, person=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='Leonardo DiCaprio', attrByQName={}}`
	2 | `movie=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=James Cameron, title=Titanic}}, person=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='Kate Winslet', attrByQName={}}`
	3 | `movie=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}, person=XMLNode{id=18, namespace='null', prefix='null', localName='LeadActor', text='Tom Hanks', attrByQName={}}`
	4 | `movie=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}, person=XMLNode{id=19, namespace='null', prefix='null', localName='LeadActress', text='Robin Wright', attrByQName={}}`

6. Read cast from all movies (including franchises)
	
	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	
	Deque<XMLNode> movies = new ArrayDeque<>();
	NodeListener castListener = (cast, person) -> {
		XMLNode movie = movies.peekLast();
	
		// your code here
	};
	
	SlurpNode cast = xmlSlurper.getNodes("**", "Movie", "Cast");
	cast.findAll((movie, cast) -> movies.addLast(movie), (movie, cast) -> movies.removeLast());
	cast.node("*").findAll(null, castListener);
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	For franchises movies, which are one level deeper in the tree that the rest of the movie nodes, we need to broader the search scope with descendants (`**`). Also benefiting from the fact that the parsing is sequential, we can utilise `Deque` to easily gain information regarding the movies current cast belongs to. As with the previous example, we're excluding start node events on children nodes of the cast nodes, however this time, we're ensuring no overflowing occurs on the `Deque` in case of processing of very large xml files.
	
	The following table provides a list of all triggered events on 'castListener' in order:
	
	Event Id | Data available
	--- | ---
	1 | `movie=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=James Cameron, title=Titanic}}, person=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='Leonardo DiCaprio', attrByQName={}}`
	2 | `movie=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=James Cameron, title=Titanic}}, person=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='Kate Winslet', attrByQName={}}`
	3 | `movie=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t', attrByQName={director=Rob Cohen, title=The Fast and the Furious}}, person=XMLNode{id=8, namespace='null', prefix='null', localName='LeadActor', text='Vin Diesel', attrByQName={}}`
	4 | `movie=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t', attrByQName={director=Rob Cohen, title=The Fast and the Furious}}, person=XMLNode{id=9, namespace='null', prefix='null', localName='LeadActress', text='Michelle Rodriguez', attrByQName={}}`
	5 | `movie=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t', attrByQName={director=John Singleton, title=2 Fast 2 Furious}}, person=XMLNode{id=12, namespace='null', prefix='null', localName='LeadActor', text='Vin Diesel', attrByQName={}}`
	6 | `movie=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t', attrByQName={director=John Singleton, title=2 Fast 2 Furious}}, person=XMLNode{id=13, namespace='null', prefix='null', localName='LeadActress', text='Eva Mendes', attrByQName={}}`
	7 | `movie=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}, person=XMLNode{id=18, namespace='null', prefix='null', localName='LeadActor', text='Tom Hanks', attrByQName={}}`
	8 | `movie=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}, person=XMLNode{id=19, namespace='null', prefix='null', localName='LeadActress', text='Robin Wright', attrByQName={}}`

7. Read second movie in the 'Fast and Furious' franchise and end parsing.
	
	```java
	XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
	xmlSlurper.getNodes("**", "fr:Franchise", "Movie").get(2).find(null, (franchise, movie) -> {
		// your code here
	});
	xmlSlurper.parse(new FileInputStream("samplefile.xml"));
	```
	
	The following table provides a list of all triggered events in order:
	
	Event Id | Data available
	--- | ---
	1 | `franchise=XMLNodeImpl{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attributeByQName={fr:title=Fast and Furious, xmlns:fr=http://franchise}}, movie=XMLNodeImpl{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attributeByQName={director=John Singleton, title=2 Fast 2 Furious}}`
	

# XMLSpitter

A convenient API for writing XML documents using `XMLNode` objects which can be either created via `XMLNodeFactory` or obtained from the documents currently being parsed, hence allowing developers to split/overwrite the existing documents.

## Usage

### Design

`XMLSpitter` utilizes StAX `XMLStreamWriter` for writing XML documents. Provided API is similar to the one available in `XMLStreamWriter`. An instance of `XMLSpitter` can spawn multiple instances of `XMLStream` which do the actual write. To write a start element simply use the method `XMLStream.writeStartElement()`, to append text - `XMLStream.writeCharacters()`, end element - `XMLStream.writeEndElement()`, to write the complete start/end element with text use `XMLStream.writeElement()`. Writing whole elements with method `XMLStream.writeElement()` has a drawback of not being able to nest elements inside the one currently being written, it should be used with causion for the purpose of writing eg. simple text containing elements with no nested descendants.

### Examples

#### Movie Database sample xml file

```xml
<?xml version="1.0" encoding="UTF-8"?>
<MovieDb xmlns="http://movieDb">
	<Movie title="Titanic" director="James Cameron">
		<Cast>
			<LeadActor>Leonardo DiCaprio</LeadActor>
			<LeadActress>Kate Winslet</LeadActress>
		</Cast>
	</Movie>
	<fr:Franchise fr:title="Fast and Furious" xmlns:fr="http://franchise">
		<Movie title="The Fast and the Furious" director="Rob Cohen">
			<Cast>
				<LeadActor>Vin Diesel</LeadActor>
				<LeadActress>Michelle Rodriguez</LeadActress>
			</Cast>
		</Movie>
		<Movie title="2 Fast 2 Furious" director="John Singleton">
			<Cast>
				<LeadActor>Vin Diesel</LeadActor>
				<LeadActress>Eva Mendes</LeadActress>
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

#### Scenarios

1. Split the document by the 'Movie' elements, produce n-documents each having one 'Movie' element
	
	```java
	XMLSpitter xmlSpitter = XMLSpitterFactory.getInstance().createXMLSpitter();
    Deque<XMLStream> streams = new ArrayDeque<XMLStream>();

    XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
    xmlSlurper.getNodes("**", "Movie").findAll((parent, movie) -> {
		try (FileOutputStream fos = new FileOutputStream("movie" + movie.getId() + ".xml")) {
			XMLStream stream = xmlSpitter.write(fos, "1.0", "UTF-8");
			stream.writeStartElement(node);

			streams.addLast(stream);
		}
	}, (parent, movie) -> {
		XMLStream stream = streams.removeLast();

		stream.writeEndElement();
		stream.flush();
		stream.close();
	});

	xmlSlurper.getNodes("**", "Movie", "**").findAll((movie, node) -> streams.peekLast().writeStartElement(node), (movie, node) -> {
		XMLStream stream = streams.peekLast();

		stream.writeCharacters(node.getText());
		stream.writeEndElement();
	});

	try {
		xmlSlurper.parse(new FileInputStream("samplefile.xml"), new File("samplefileSchema.xsd"));
	} catch (ParserConfigurationException | SAXException | IOException e) {
		// handle XMLSlurper related exceptions
	} catch (XMLStreamRuntimeException e) {
		// handle XMLStream related exception
	}
	```
	
	The above snippet of code is going to extract and create not only immediate children 'Movie' nodes but also the ones placed under 'fr:Franchise' node. By providing a XSD schema file for the XML document ignorable characters (indentations/carriage returns etc. in the input document) are going to be truly ignored. Notice, handling of `XMLStream` related exceptions occurs on `XMLSlurper.parse()` method. `XMLStreamException`, produced by the `XMLStream`, has been wrapped in `XMLStreamRuntimeException` inheriting from `RuntimeException` for convenience and still should be caught and handled.
	
	Output of one of the generated 'Movie' XML documents is as follows:
	
	movie1.xml:
	
	```xml
	<?xml version="1.0" encoding="UTF-8"?><Movie xmlns="http://movieDb" title="Titanic" director="James Cameron"><Cast><LeadActor>Leonardo DiCaprio</LeadActor><LeadActress>Kate Winslet</LeadActress></Cast></Movie>
	```
	
	Namespace related information is written automatically. Formatting information is absent, it is up to the developer to include formatting code to the XML document generator/splitter.