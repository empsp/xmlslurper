Java XMLSlurper
===============

An attempt to port parsing capabilities offered by Groovy XMLSlurper into Java world. The following is not planned to be accurate projection, instead the most useful functions will be implemented.

## Overview

The basic premise is to facilitate xml file parsing by combining XPath/GPath similar data search with event based stream processing. XMLSlurper utilizes SAX parser to perform bulk of the operations. Event appearance will align with what SAX supports, data will be provided sequentially ordered. XMLSlurper wraps acquired information into a convienent `XMLNode` which is available through `NodeListener` functional interface (compatible with Java 1.8 Lambda expressions). The library has been compiled against Java 1.6, hence can be used in legacy code.

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

The following is a list of XMLSlurper capabilities:

1. Read all the elements (with attributes and text) from the xml file.
2. Read all the elements that match the given path.
3. Read all the elements that contain the given attribute.
4. Read all the elements that have the given attribute with specific value/values different than/value matching given regex expression.
5. Read all the elements that are siblings (`*`) under a given elements.
6. Read all the elements that are descendants (`**`) of a given elements.
7. Combine the siblings (`*`) and descendants (`**`) to gain even more fine-grained search results.
8. All of the above except that n-th elements will be choosen that match the given path/attribute/value with respect to siblings (`*`) and descendants (`**`).
9. Or, a single first/n-th element will be choosen that match the given path/attribute/value with respect to siblings (`*`) and descendants (`**`). After the element is provided, the parser will break further xml file processing.

All of the above will return searched nodes together with parent nodes of those nodes. This way, the developers have the possibility to deduce where the node is placed within the descendants tree of the xml file. The returned information will be split among two events, start node and end node events. End node events contain additional information regarding nodes' text. The developer can decide if both events are of his interest, or only a start node or only an end node.

Additionally the library ensures that:

1. `@NotNull`/`@Nullable` interfaces will be adhered to.
2. XMLSlurper will release all of the resources after the execution (including the `InputStream` given).
3. Single XMLSlurper instance can be reused many times, however the paths must be redefined.
4. `XMLNode` objects are meaningfully equal only by the ids which will be unique only to the scope of a single xml file processing. Consecutive parsing of a different xml file will produce objects with different data but with matching ids. Therefore `XMLNode` objects are not fit and designed to be stored, instead the information should be extracted and the objects should be left for garbage collection.
5. Namespace awareness can be turned on/off (feature turned on by default).

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
	1 | `parent=null, node=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='null', attrByQName={}}`
	Comment | The first event is a start node event on a root node. In such a case, parent will have a `null` value.
	2 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={title=Titanic, director=James Cameron}}`
	Comment | 'MovieDb' parent node's text has been updated with new line and spaces (here symbolized by tab `\t` character) that there are before the start of a 'Movie' node.
	3 | `parent=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={title=Titanic, director=James Cameron}}, node=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='null', attrByQName={}}`
	Comment | 'Movie' parent node's text has been updated with white characters as well.
	4 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}}, node=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='null', attrByQName={}}`
	Comment | As a start node event, the text value of a 'LeadActor' node is still `null`.
	5 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t', attrByQName={}}, node=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='Leonardo DiCaprio', attrByQName={}}`
	Comment | Here, at end node event, the text value of the 'LeadActor' node is finally available.
	6 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}}, node=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='null', attrByQName={}}`
	Comment | Another start node event, and the text value of a 'LeadActress' node is `null`, however the text value of the 'Cast' parent node has been updated with additional white characters.
	7 | `parent=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t', attrByQName={}}, node=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='Kate Winslet', attrByQName={}}`
	8 | `parent=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={title=Titanic, director=James Cameron}}, node=XMLNode{id=2, namespace='null', prefix='null', localName='Cast', text='\n\t\t\t\n\t\t\t\n\t\t', attrByQName={}}`
	9 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t', attrByQName={title=Titanic, director=James Cameron}}`
	10 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t', attrByQName={}}, node=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='null', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}`
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
	1 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=James Cameron, title=Titanic}}`
	2 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t', attrByQName={director=James Cameron, title=Titanic}}`
	3 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`
	4 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t\t\n\t\t\n\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`

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
	1 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=James Cameron, title=Titanic}}`
	2 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t', attrByQName={}}, node=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t', attrByQName={director=James Cameron, title=Titanic}}`
	3 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Justin Lin, title=Fast and Furious 6}}`
	Comment | 'Franchise' node is only available here as a parent node. It has not been found as a node because it has a 'title' attribute within different namespace.
	4 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attrByQName={director=Justin Lin, title=Fast and Furious 6}}`
	5 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=James Wan, title=Furious 7}}`
	6 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attrByQName={director=James Wan, title=Furious 7}}`
	7 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`
	8 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t\t\n\t\t\n\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`
	
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
	1 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t', attrByQName={}}, node=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='null', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}`
	Comment | Here, 'Franchise' node is a result node having a parent node 'MovieDb'.
	2 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t', attrByQName={}}, node=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t\n\t\t\n\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}`

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
	1 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Justin Lin, title=Fast and Furious 6}}`
	2 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attrByQName={director=Justin Lin, title=Fast and Furious 6}}`
	3 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=James Wan, title=Furious 7}}`
	4 | `parent=XMLNode{id=5, namespace='http://franchise', prefix='fr', localName='Franchise', text='\n\t\t\n\t\t', attrByQName={fr:title=The Fast and the Furious, xmlns:fr=http://franchise}}, node=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t\n\t\t', attrByQName={director=James Wan, title=Furious 7}}`
	5 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='null', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`
	6 | `parent=XMLNode{id=0, namespace='null', prefix='null', localName='MovieDb', text='\n\t\n\t\n\t', attrByQName={}}, node=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t\n\t\t\n\t\t\n\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}`

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
	Please note, for very large xml files, the above construct requires grooming of the `HashMap` to protect it from `java.lang.StackOverflowError`.
	
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
	
	For franchises movies, which are one level deeper in the tree that the rest of the movie nodes, we need to broader the search scope with descendants (`**`) marking. Also benefiting from the fact that the parsing is sequential, we can utilise `Deque` to easily gain information regarding the movies current cast belongs to. As with the previous example, we're excluding start node events on children nodes of the cast nodes, however this time, we're ensuring no overflowing occurs on the `Deque` in case of processing of very large xml files.
	
	The following table provides a list of all triggered events on 'castListener' in order:
	
	Event Id | Data available
	--- | ---
	1 | `movie=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=James Cameron, title=Titanic}}, person=XMLNode{id=3, namespace='null', prefix='null', localName='LeadActor', text='Leonardo DiCaprio', attrByQName={}}`
	2 | `movie=XMLNode{id=1, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=James Cameron, title=Titanic}}, person=XMLNode{id=4, namespace='null', prefix='null', localName='LeadActress', text='Kate Winslet', attrByQName={}}`
	3 | `movie=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t', attrByQName={director=Justin Lin, title=Fast and Furious 6}}, person=XMLNode{id=8, namespace='null', prefix='null', localName='LeadActor', text='Vin Diesel', attrByQName={}}`
	4 | `movie=XMLNode{id=6, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t', attrByQName={director=Justin Lin, title=Fast and Furious 6}}, person=XMLNode{id=9, namespace='null', prefix='null', localName='LeadActress', text='Michelle Rodriguez', attrByQName={}}`
	5 | `movie=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t', attrByQName={director=James Wan, title=Furious 7}}, person=XMLNode{id=12, namespace='null', prefix='null', localName='LeadActor', text='Vin Diesel', attrByQName={}}`
	6 | `movie=XMLNode{id=10, namespace='null', prefix='null', localName='Movie', text='\n\t\t\t', attrByQName={director=James Wan, title=Furious 7}}, person=XMLNode{id=13, namespace='null', prefix='null', localName='LeadActress', text='Michelle Rodriguez', attrByQName={}}`
	7 | `movie=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}, person=XMLNode{id=18, namespace='null', prefix='null', localName='LeadActor', text='Tom Hanks', attrByQName={}}`
	8 | `movie=XMLNode{id=16, namespace='null', prefix='null', localName='Movie', text='\n\t\t', attrByQName={director=Robert Zemeckis, title=Forest Gump}}, person=XMLNode{id=19, namespace='null', prefix='null', localName='LeadActress', text='Robin Wright', attrByQName={}}`
