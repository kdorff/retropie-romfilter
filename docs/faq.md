---
layout: page
title: FAQ
menu: main
permalink: /faq/
---

**Q:** When I first start the app it takes forever before it responds.

**A:** Update! The latest version of the application supports scanning on-demand under Systems and Jobs.

**Q:** Things changed in the filesystem or I have re-scraped but I don't see the changes in romfilter

**A:** Re-scan your systems under Systems and Jobs.

**Q:** I'm having a problem

**A:** Wait for the app to become more stable. Try stopping the app, deleting the index folder in the app's directory (romfilter-games.index), and restarting the app. You will need to re-scan your systems (Systems and Jobs).

**Q:** Should I open this port up in my router so I can browse my roms while I am away from home or let me cousin look at it?

**A:** No. This app has no security baked in. Don't do it. Run this on your private network, only.

**Q:** Is the 'hash' the hash of the ROM?

**A:**: No. It is a unique number based on a combination of other fields in the entry (path, system, rom file size).

**Q**: Why did you make this app?

**A**: I started to extend RetroPie-Manager (I made a PR) but it seems that that project has been mostly
abandoned. I'm not well versed enough in Python / Django to become the new maintainer.
After playing with Django for a few hours, I was convinced I could just make the app I needed with
far less effort than augmenting (and maintaining) the Django app.
I'm a Groovy / Grails programmer by day, so it was a natural choice.
The features I needed I knew how to develop with Groovy and Grails where I didn't
feel confident I could do what I wanted this app to do in PHP or Python in someone else's project,
given my level of experience in those languages (and lack of libraries?).
And this search is amazing.
