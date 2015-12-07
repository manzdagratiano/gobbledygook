# gobbledygook

The Unhackable Password Solution

## Houston, We have a Problem!

Ever enter a password on a website and have it emailed back to you in
cleartext? Suddenly, your precious password has been compromised (i.e. the
"secret" is not a secret anymore)! If you were using this password on any
other website, you suddenly find yourself in a frenzy to rush and change it
everywhere! Life is suddenly topsy-turvy for a few minutes.

Most of us have a few passwords which we recycle everywhere. The problem of
having/managing different passwords for every website is a **colossal
nightmare**. To solve this problem, we need a **paradigm shift** - instead
of **storing** funky new passwords, we need a **reliable**, and
**reproducible** way of **generating** passwords - reproducible for
the user and for no one else - and **secure** at that in the Cryptographic
sense.

## What is this "gobbledygook"?

`gobbledygook` proposes to solve this problem by placing the power of
mangling/strengthening your password in your own hands. It is a plugin
within your browser, designed so that you can *keep* your secret, and still
allow it to strengthen it many-fold to present to the page.

When you need to enter a password on a webpage, instead of entering your
*real* password within the dialog box, you click on gobbledygook, enter your
password there, hit "Generate!", and *voila!* Your "mangled" (strengthened,
or technically, "key-stretched") *proxy password* is copied to your
clipboard - ready to be pasted in the dialog box for the website.  The
burden to you is **two extra clicks** of the mouse followed by a "Ctrl+v".
The strengthened password will **always be the same**, and is **never
saved**, **nor** is your original password (more below on what actually
needs to be saved **once**).

For instance, a password like `foo` (not that you should use that as your
password) can become something like:
`5kxpfzVY1mGOLybEg1n11IUJIt9TnWeKjKTPSkXTu6c`

A **breeze** for you, a **nightmare** for the attacker.

(The above example is not a "truth", i.e., "foo" will not lead to the above
password for any other user, **ever**. There is an extra user-specific
element involved - more on that below.)

You can reuse your **one true password** across websites, and if a domain is
compromised and its security practices allow your **proxy password** to be
revealed (plaintext/weak hash), your original password cannot be deciphered
by an attacker targeting that page.  The damage is contained to **that
domain only**, and easily mitigated (more later).

**And yet, we don't save no "secrets".**

## Using Gobbledygook

It's very simple!

Whatever web page you are on, when you need to enter a password, click on `gobbledygook`, enter your **one true password**, and hit "Generate!". Your proxy password has been generated and copied to the clipboard. Paste it where you need it.

Under the hood, Gobbledygook generates a **salt generating key** for you on
the first run, and sets the default number of iterations to 10000. You can
tweak these settings for the extension (by clicking on the settings icon
in the Gobbledygook UI or through the browser's extension menu).

Needless to say, **if the salt generating key changes, so will the
generated passwords**. To prevent accidental changes to the key while
examining options, the following **rudimentary safeguards** towards
1D107-proofing are built in:


### Advanced Options

The main Gobbledygook UI keeps things simple - it generates the proxy
password based on the salt key and the default number of iterations. However,
you can access the "Advanced" options for each run where you can:
- Override the number of iterations for the run,
- Optionally, if there is a length restriction on the password (why should
  there be?), specify a **truncation** before hitting "Generate!",
- Choose to save any custom overrides from the run for future use.

- Firefox requires the "Unlock Salt Key" radio button to be set to "Unlock" to generate the key using "Generate Key", after which it automatically locks it again (this does **not** prevent manual changes, which is by design - e.g., if you are importing an existing key from elsewhere),
- Chrome will only save the new key (generated using "Generate Key" or updated manually) IFF "Save Options" is clicked after generating the key.

This is why **backing up the key** elsewhere, once generated, is also
recommended.  This key need not be regarded as a "**secret**" - it exists
purely to make salts globally unique to avoid **salt clustering** and
precomputed **rainbow table** attacks.

## Tweaking

You can override the "**work factor**" (which is the number of PBKDF2
iterations - the mechanism by which your password is "stretched") to be used for `gobbledygook`
globally. It is set to 10000 by default for performance reasons across
machines old and new, but if your machine is capable of supporting a much
higher count, you can, and by all means **should**, set it so (note that
there are **two** PBKDF2 hash operations for each domain - for more details
on the internals, see the
[Documentation](https://manzdagratiano.github.io/gobbledygook)).

You can also override the defaults **per domain**, if necessary:

- The **domain** itself
- The number of **iterations** for that domain
- A **truncation**, if necessary, for the domain.

To do so, simply check "Save Preferences" before hitting "Generate!". A
check mark will be displayed next to it if the save was successful, and a
cross otherwise. No action is taken if there is no information different
from the defaults, even if the checkbox is checked.

The per-domain override data is stored as a **JSON string** with keys as the
original domain name, and values "<domain name>|<iterations>|<truncation>".
To minimize storage space, **only the overridden values are saved** - for
example, if you only override the truncations for "foobar.com" to say, 1337,
the saved data will look like `"foobar.com":"|1337|"`, with empty
placeholders for the other attributes. The entire string is available for
inspection/modification in the add-on options page. It must be a **valid
JSON string** at all times.

The per-domain override should be used **sparingly** - all the data saved by
`gobbledygook` is **synced** using the browser's **native** sync mechanism
(there is no other server - the author is neither interested in maintaining
nor analyzing users' data), which has size limitations. It should be
sufficient for a large number of sites, but an unsuccessful save attempt
will be communicated, as before, with a cross mark. If you find yourself
doing an override for every domain, you're doing it wrong.

## Installing the Plugin

The production version of the plugin can be installed through the browser's
official distribution channel:

- Firefox: [Add-ons for Firefox] (https://addons.mozilla.org/en-US/firefox/addon/gobbledygook)
- Chrome: [Chrome Web Store](https://chrome.google.com/webstore/detail/gobbledygook/dolcdnkkojbooecjddceiojblpbohkgd?hl=en&gl=US)

## Building

For development and testing, on Unix-like systems, `gobbledygook` may be
built for Firefox/Chrome by issuing the following in the project root
directory.

- `make firefox`
- `make chrome`

The unpackaged extensions will be generated under `target/firefox` and
`target/chrome` respectively.

The `Makefile` simply assembles the respective targets by moving common
components to their requisite locations.

- For Firefox, run the extension using `cfx run` in the `target/firefox` directory (needs the Firefox [Add-on SDK](https://developer.mozilla.org/en-US/Add-ons/SDK)),
- For Chrome/Chromium, load the extension by enabling "Developer Mode" on the extensions page, followed by "Load unpacked extension...".

## What if a Domain gets Compromised?

This is what `gobbledygook` exists for! Simply override the **work factor**
for that domain and check the "Save Preferences" box in the UI, and you're
all set! Since the attacker for that domain does not know your salt
generator key, nor the original work factor if you overrode it globally,
cracking your compromised **proxy password** further is a near-impossible
task (PBKDF2-HMAC-SHA256 is also **pre image resistant**, so this technique
works well).

## Documentation

Documentation about the internals of `gobbledygook`, as well as function
documentation, is available at
<https://manzdagratiano.github.io/gobbledygook>.
