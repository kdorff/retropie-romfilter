---
layout: page
title: Searching
menu: main
permalink: /searching/
---

Since the focus of this application is browsing your ROM collection, here are some
example searches to help illustrate how to use the application:

```
tempest             | Find the term tempest in any field
"super mario"       | Find the phrase 'super mario' (the term super followed by the term mario) in any field
name:"pac man"      | Find the phrase 'pac man' (the term 'pac' followed by the term 'man' in the name field
name:pac man        | Find the term 'pac' in the name field OR the term 'man' in any field (probably not what you wanted)
+name:pac +name:man | Find the term 'pac' in the name field AND the term 'man' in the name field
```

Fields names you can use for search prefixes

```
system, size, name, desc, developer, publisher, genre, players,
region, romtype, releasedate, rating, playcount, lastplayed,
scrapeId, scrapeSource, hash
```