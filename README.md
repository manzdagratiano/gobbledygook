# gobbledygook

Annihilate Password Fatigue!

## Houston, We have a Problem!

Ever enter a password on a website and have it emailed back to you in cleartext? Suddenly, your precious password has been compromised (i.e. the "secret" is not a secret anymore)! If you were using this password on any other website, you suddenly find yourself in a frenzy to rush and change it everywhere! Life is suddenly topsy-turvy for a few minutes.

\<**soapbox**\>
The future is **here** - we are living in it. The dystopian scenarious of **Orwell** and **Dick** do not appear to be so far-fetched anymore - **Big Brother** is indeed watching us, and **Androids** do dream of Electric Sheep. **Cyberpunk** is very much a reality. Your interaction with the World Wide Web can and is being exploited in ways you could not have imagined! Security on the web is therefore, paramount. And this security inevitably revolves around your password.
\<**/soapbox**\>

Most of us have a few passwords which we recycle everywhere. It is not humanly possible to remember passwords for hundreds of different websites. The traditional way to resort to solving this problem has been the following:

- **Write down** all your passwords somewhere and update this list as you go (inefficient, not portable),
- **Allow the browser** to save every password - passwords are unlocked using a master password; if you use different passwords for each website and this database is lost/reset/not synced (computers crash) - you are left stranded.
- **Use a third party SaaS** to manage passwords for you. If this third party is closed source, this approach directly violates **"Trust No One" (TNO)**.  There are three threat models that could occur in this scenario:
    - your connection with the SaaS is hijacked by a **man in the middle**, who steals your secret in transit,
    - the third party employs **less-than-optimal security pratices**, and gets compromised at some point in time, thereby compromising you,
    - the third party is **itself malicious**, and uses your password to exploit websites you would sign in with that password.

All of the above models require either a **compromise** of convenience, or of TNO. If you get one, you are **forced** to compromise the other.

## What is this "gobbledygook"?

`gobbledygook` proposes to solve this problem by placing the power of mangling/strengthening your password in your own hands. It is a plugin within your browser. When you need to enter a password on a webpage, instead of entering your *real* password within the dialog box, you click on gobbledygook, enter your password there, hit "Generate!", and voila! Your "mangled" (strengthened, or technically, "key-stretched") password is copied to your clipboard and ready to be pasted in the dialog box for the website.  The burden to you is **two extra clicks** of the mouse followed by a "Ctrl+v". The mangled password will **always be the same**, and is **never saved**, **nor** is your original password (more below on what actually needs to be saved **once**).

The design goals of gobbledygook are to find the best possible pivot for
balancing:
- **convenience**
- **TNO**

It exists as a browser plugin precisely for the first goal - the more things someone needs to do to get it to work for them, the less they will tend to use it. Making it a desktop app instead creates the problem of portability, installability etc etc. Having it exist as a plugin for the browser allows it to be ready for deployment on any desktop/OS that allows installation of the browser.

The second goal is to protect you from the threat models identified in the problem. How do we solve TNO? The problem arises when you trust someone with your "secret". `gobbledygook` solves this problem by letting you **keep your secret** - i.e., it **does not store** your password in any shape or form. In fact, it does not even **_see_** your password - your password is directly hashed to SHA256, and only then used anywhere else in the program. Even in this form, it is not logged anywhere at the default logging level in the console logs.

## Using gobbledygook

To use `gobbledygook`, start with the "Options" (in Chrome, this is "More tools"-\>"Extensions"-\>"Options" for the add-on, while in Firefox, this is "Tools"-\>"Add-ons"-\>"More" for the add-on), and "**Generate Key**" to create the salt key (this needs to be done only once). To prevent accidental regeneration of the key, Chrome will save the key IFF "Save Options" is clicked subsequently, while Firefox requires the "Unlock Salt Key" radio button to be set to "Unlock" to generate the key, after which it automatically locks it again (this does **not** prevent manual changes) - these measures are designed to make the key somewhat 1D107-proof. This is why saving the key elsewhere, once generated, is also recommended.

You can also override the "work factor" to be used for `gobbledygook` globally. It is set to 10000 by default for performance reasons across all machines, but if your machine is capable of supporting a much higher count (note that there are **two** PBKDF2 hash operations for each domain), you can, and should, by all means set it so.

Once the key is generated, whenever you are required to enter a password on any domain, click on the `gobbledygook` add-on, enter your password there, hit "Generate!" to obtain your proxy password (copied to clipboard as well), and paste it in the domain password field. **No new information is saved** (unless you are overridding a default, which should normally be done only in the event of a compromise; if you find yourself overridding every domain, you are doing it wrong, and will also eventually run out of your "sync" allowance in the browser).

## How it Works

The algorithm, in a nutshell, is rather simple. First, create a **salt-generating key** from a CSPRNG, encoded in base64. Subsequently, for any domain:

- Obtain the user's "**one true password**" as a **SHA256 hash**,
- Generate a **salt** for the domain by passing the domain name with the "salt key" through a large number of rounds of **PBKDF2-HMAC-SHA256**,
- Generate a **proxy password** using the salt to stretch the SHA-256 hashed password through **PBKDF2-HMAC-SHA256** for a large number of rounds, encoding the result into **urlsafe base64**. Optionally, truncate this to a shorter length if the website has a length restriction for the password (why should they?).

The salt generator key is created from 512 bytes of random data generated from a CSPRNG, and encoded with base64 to obtain 684 utf-8 characters (in the set \[A-Z\]\[a-z\]\[0-9\](-,\_)) with padding. This key is then stored and used to generate a salt for every domain visited. This key will be synced across browser instances, but users should save it elsewhere as well for good measure (if it is lost, all the generated passwords become irreproducible).

The resulting proxy password has a maximum length of 44 characters (padding ='s are stripped off), and looks just like **mangled text** - each character belongs to the set \[A-Z\]\[a-z\]\[0-9\](-,\_). There are no other special characters (some websites don't allow them anyway - again, why wouldn't they?), but with this scheme, the "brute-force" factor per character is 10<sup>64</sup>, which is already obscenely large with this set. Common substitution based mechanisms following this hashing do not provide any significant additional security (they're easily broken by modern crackers, which allow for such substitution rulesets). Note that your original password can contain **any** utf-8 character; it's just that the output set is restricted due to the nature of algorithm.

Why do we have a unique **salt generator key** instead of generating salts from a CSPRNG every single time? The reason for this boils down to **convenience** and **portability**; there are a very small number of things to remember across domains as well as platforms - this key itself, and the work factor (if customized) - while generating salts from the CSPRNG every time would add a lot more **state** to the addon, requiring syncing a lot more information across platforms/devices. The above scheme for generating salts guarantees that the salt is still **globally unique** per domain per user, and successfully avoids the problem of **salt clustering**, which facilitates **rainbow table attacks**. Since the key is generated using a CSPRNG, it is **not possible** to create rainbow tables in advance in anticipation of this scheme. Note that this "key" is **not a secret**; it exists purely for the same purpose salts themselves exist - to thwart the pre-creation of rainbow tables. It is therefore also stored in plaintext (storing it encrypted would result in a **chicken-egg problem**). If an attacker were to get a hold of this key for a user, they would be able to precompute the salt for every domain for that user; this is a moot point, since in the alternative model, the attacker would have access to all the salts anyway with the same kind of attack.

## Threat Models

`gobbledygook` allows you to have just the "**one true password**", and still create a unique password per domain. Effectively, the mangled password becomes *your password* for that domain. If an attacker can gain this "proxy" password **for a domain**, they have by all means gained access to your account **for that domain**, but **nowhere else**, while in the original case, they would have everywhere you used the same password. `gobbledygook` was not designed to address the **man in the middle** attack problem, but rather to **limit your exposure** to such attacks by solving the problem of **password fatigue**. It is still a burden on the domain to prevent such an attack when interacing with the user.

Many websites try to do a client-side hashing before transmitting data so that the user's bare password is never transmitted at all. In a perfect world, this would render `gobbledygook` redundant, but in practice it does not:

- Websites that do this are **still rare** as of 2014,
- You **cannot implicitly trust** a website you log in to unless you can see the entire client-side code,
- Secure delivery of client-side crypto from the server is a **chicken-egg problem** (more below).

The purpose of `gobbledygook` is to shield you from all of these by securing your password at **your end** - before you begin interaction with a website; they cannot betray your secret if they do not know it to begin with.

## Choice of Crypto

The Crypto library chosen for the implementation of the algorithm is the [Stanford Javascript Cryptography Library (SJCL)](https://github.com/bitwiseshiftleft/sjcl). It has an easy to use interface and is very well performant, also providing a CSPRNG interface (which can be trusted as long as the underlying runtime can be trusted). `gobbledygook` ships with its own minimal version of the library to avoid the issue of secure delivery of crypto over a potentially compromised channel.

## What about '[JavaScript Cryptography Considered Harmful](http://matasano.com/articles/javascript-cryptography/)'?

**We're absolved** (for the *most* part).

Why? Most of the issues with cryptography and JavaScript in the browser revolve around the **secure delivery** of the crypto code from the **server** to the **client**, which is a **chicken-egg problem**. In our case, there **_is_** no server, and the addon ships with **its own version of the crypto code**, running in its own sandboxed environment and making no http requests, thus being no different from any regular application running on the client's desktop. The only remaining snag is the **verifiability of the JavaScript runtime**, but if your runtime is compromised, you probably have bigger issues to worry about than `gobbledygook`.

## What if a Domain gets Compromised?

This is what `gobbledygook` exists for! Simply override the **work factor** for that domain and check the "Save Preferences" box in the UI, and you're all set! Since the attackers for that domain do not know your salt generator key, nor do they know the original work factor, cracking your compromised hash, even if any server-side hashing on top is weak and crackable, is a near-impossible task.

## Building

`gobbledygook` will be distributed through the official browser channels - for Firefox, this is <https://addons.mozilla.org>, while for Chrome/Chromium, this is the [Chrome Web Store](https://chrome.google.com/webstore/category/extensions). For development and testing, on Unix-like systems, `gobbledygook` may be built for Firefox/Chrome by issuing the following in the project root directory.

- `make firefox`
- `make chrome`

The unpackaged extensions will be generated under `target/firefox` and `target/chrome` respectively. The `Makefile` simply assembles the respective targets by moving common components to their requisite locations. For Firefox, the extension may be run using `cfx run` in the `target/firefox` directory, while for Chrome/Chromium, it may be loaded by enabling "Developer Mode" on the extensions page, followed by "Load unpacked extension...".

## Documentation

API documentation for `gobbledygook` was generated using [JSDoc 3](usejsdoc.org), and is available at <https://manzdagratiano.github.io/gobbledygook>.
