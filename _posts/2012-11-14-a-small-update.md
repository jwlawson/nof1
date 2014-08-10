---
layout: post
title: A Small Update
---

# A small update

I finished work on this project at the end of September. At this point I had got
a working website, the app was pretty much complete and could upload the patient
data to our website so their doctor can view progress online. Since then I have
resumed my degree study and as such have had little chance to work on the
project.

Over the past month or so I have added the ability to upload questionnaires to
our server, to that the doctor does not have to input lots of questions on the
patient's device. This feature was suggested to me by Jane Nickles from the
University of Queensland, who expressed an interest in using our app.
I have also fixed a few problems I had with the data uploading. There were two
main problems here: the first of which was that I had not correctly set up the
Android Account Manager so when the user had to provide access to their account
the app did not offer them this option and hence no data was uploaded. This then
caused a large amount of data to collect in the cache waiting to be uploaded but
failing to actually upload. The caching system I used ran into concurrency
problems which ultimately overwrote some of the data which was meant to be
uploaded and so it was lost. Solving these required a fairly large rewrite of
the code handling the data upload, but the solution I came up with is an awful
lot cleaner than what it was originally.

I am also presenting the work at the IDH (Institute for Digital Healthcare)
conference on the 21st November which will be fun and hopefully will provide
some interesting feedback.

