# Gobbledygook

T3h Unh@X@b13 P@$$w0rd $01ut10n

## Houston, We have a Problem!

Tired of having your password compromised on a popular website every
other month? If you were using this password on any other website, you
suddenly find yourself in a frenzy to rush and change it everywhere!
Life is suddenly all topsy-turvy...

Ever enter a password on a website and have it emailed back to you in
cleartext? There goes your precious password! Oh my, did you use it on
any other websites? You are done for!

Most of us have a few passwords which we recycle everywhere. The problem of
having/managing different passwords for every website is a **colossal
nightmare**. To solve this problem, we need a **paradigm shift** - instead
of **storing** funky new passwords, we need a **reliable**, and
**reproducible** way of **generating** passwords - reproducible for
the user and for no one else - and **secure** at that in the Cryptographic
sense.

## What is this "Gobbledygook"?

Gobbledygook puts and end to this madness, and allows you to reclaim your
password. Starting with a **single password**, it allows you to generate a
**unique password** for every website you visit...

You're like, "*Aren't there a million other solutions to this?*"

Here's the catch - **Gobbledygook does not store anything.** Not your real
password, not the generated password, nothing (*almost!*).

What about the "*almost*"?

On first run, Gobbledygook generates a **unique key** for you, which will be
used to mangle your passwords on every website you visit. You still enter a
password every time, but instead of in the password dialog of the website, you
do that in Gobbledygook, and hit "Generate!". *Et Voila!* Your proxy password
has been generated and copied to the clipboard. Paste it where you need it.
(*While Chromium/Chrome work as expected, automatic copy is broken in Firefox
webextensions in the current version.*)
[Firefox support is fixed and will appear in a later version of Firefox.](https://bugzilla.mozilla.org/show_bug.cgi?id=1197451).

The burden to you is **two extra clicks** of the mouse followed by a "Ctrl+v".
For instance, a password like `foo` (not that you should use that as your
password) can become something like:
`avp1cdi!kMX@a0mMn6EC.&!CRII{Y4TSPA4*p(@h`

A **breeze** for you, a **nightmare** for the attacker.

(*This above example is not a "truth", i.e., "foo" will not lead to the above
password for any other user, ever.*)

**We don't save no "secrets".**

### Advanced Options

The main Gobbledygook UI keeps things simple - it generates the proxy password
based on the salt key and the default number of iterations. However, you can
access the "Advanced" options for each run where you can:
- Override the number of iterations for the run,
- Choose to not use special characters,
- Optionally, if there is a length restriction on the password (why should
  there be?), specify a **truncation** before hitting "Generate!",
- Choose to save any custom overrides from the run for future use.

**Back up your settings** (You can import/export settings from/to a browser
instance, and between Firefox and Chrome). If the unique key or number of
iterations change, so will the generated password.

## What if My Settings are Stolen?

**Gobbledygook is unhackable.** Even if you settings - your unique key and the
global work factor - are stolen, they are **useless** by themselves, because
they only form **half** the puzzle. Your actual password is still unknown to
the thief. In fact, the unique key need not even be regarded as a "**secret**"
- it exists purely to make salts globally unique to avoid **salt clustering**
and precomputed **rainbow table** attacks.

## What if a Domain gets Compromised?

This is what Gobbledygook exists for! Simply override the **work factor**
for that domain and check the "Save custom overrides" box in the UI, and
you're all set! Since the attacker for that domain does not know your unique
key, nor the original work factor if you overrode it globally,
cracking your compromised **proxy password** further is a near-impossible
task (PBKDF2-HMAC-SHA256 is also **pre image resistant**, i.e., the end result
of, say, 10001 iterations cannot be derived by simply knowning the end result
of 10000 iterations, so this technique works well).

## Tweaking

You can override the "**work factor**" (which is the number of PBKDF2
iterations - the mechanism by which your password is "stretched") to be used
for Gobbledygook globally. It is set to 10000 by default for performance
reasons across machines old and new, but if your machine is capable of
supporting a much higher count, you can, and by all means should, set it so
(note that there are **two** PBKDF2 hash operations for each domain - for
more details on the internals, see the [Documentation](https://manzdagratiano.github.io/gobbledygook)).

You can also override the defaults **per domain**, if necessary:

- The **domain** itself,
- The number of **iterations** for that domain,
- Whether to not use **special characters**,
- A **truncation**, if necessary, for the domain.

To do so, simply check "Save custom overrides" under "Advanced" before hitting
"Generate!". A check mark will be displayed next to it if the save was
successful, and an "X" otherwise. No action is taken if there is nothing
different to save, even if the checkbox is checked.

The per-domain override should be used **sparingly** - all the data saved by
Gobbledygook is **synced** using the browser's **native** sync mechanism,
which has size limitations. It should be sufficient for a large number of
sites, but if you find yourself doing an override for every domain, **you're
doing it wrong.** (*The Firefox webextension API does not support syncing at the
moment. This will eventually be fixed.*)

## Installing the Extension

The production version of the plugin can be installed through the browser's
official distribution channel:

- Firefox: [Add-ons for Firefox] (https://addons.mozilla.org/en-US/firefox/addon/gobbledygook)
- Chrome: [Chrome Web Store](https://chrome.google.com/webstore/detail/gobbledygook/dolcdnkkojbooecjddceiojblpbohkgd?hl=en&gl=US)

## Building

For development and testing, on Unix-like systems, Gobbledygook may be
built for Firefox/Chrome by issuing the following in the project root
directory.

- `make firefox`
- `make chrome`

The unpackaged extensions will be generated under `build/firefox` and
`build/chrome` respectively. The unpacked extensions can be directly loaded in
Firefox (`about:debugging`) and Chrome (`Load unpacked extension`) respectively.

## Documentation

Documentation about the internals of Gobbledygook, as well as function
documentation, is available at
<https://manzdagratiano.github.io/gobbledygook>.
