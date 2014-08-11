---
layout: post
title: Services and receivers
---

Today has been a long day with a lot of things learnt along the way. I was
trying to set up the scheduling and notifications in the app, which meant making
a background service and a BroadcastReceiver. This in itself shouldn't be a
problem, but I spent far too much time either being stupid, writing stuff I
ended up binning or just trying to fix problems that weren't there. Oh well.

One problem was trying to make notifications without an icon, which are promptly
ignored by android, as they are pointless. However I didn't realise this because
the new Android development tools kindly filters out any errors that aren't
directly to do with my app. So system calls about ignored notifications were
hidden away. Nice. So lesson one, occasionally check the full logs.

Another problem was that I have two similar functions, which do very similar
things. One gets called at the very start of the trial process, the other is
repeatedly called after that. I had managed to write a bug into the first
function, but spent a lot of futile effort 'fixing' the second function, which
was working fine. It was only when I put some extra log calls in that would have
appeared no matter what the first function did, but then of course didn't, that
I realised I was working in completely the wrong area. I don't know whether this
should be a warning against having two very similar functions, and I should
maybe have merged them into one, or whether I should pay more attention to what
I am doing rather than keep adding code when things don't work.

