#!/usr/bin/env python

## @package     gobbledygook
#  @file        gobbledygook.py
#  @brief       Script to generate a "proxy" password for a domain
#               from a given user password.
#
#  @author      Manjul Apratim
#  @date        Dec 28, 2014
#
#  @license     GNU General Public License v3 or Later
#  @copyright   Manjul Apratim, 2014, 2015

# =========================================================================

"""
This script generates a "proxy" password from a given user password
for a specific domain, by key-stretching the password through PBKDF2
using a salt generated from a generator key using PBKDF2.
"""

from __future__ import print_function
import argparse
import base64
import binascii
import getpass
import hashlib
import json
import os
import sys
import zmq.utils.z85

# =========================================================================
# GLOBAL CONSTANTS

# the default number of iterations
DEFAULT_ITERATIONS = 10000
# the size of the key used for deriving the salt
SALT_DERIVATION_KEY_SIZE = 512
# the PBKDF2 key size
PBKDF2_KEY_SIZE = 32

# =========================================================================
# ROUTINES

def generate_key():
    """Function to generate a key for deriving the salt."""
    randomBytes = os.urandom(SALT_DERIVATION_KEY_SIZE)
    key = base64.b64encode(randomBytes).decode("utf-8")
    print("key={0}".format(key))
    return key

def generate_salt(domain, key, iterations):
    """Function to generate a salt from the domain name
    and the key."""

    if key is None:
        print("Generating key...")
        key = generate_key()

    saltBuf = hashlib.pbkdf2_hmac(
            'sha256',
            hashlib.sha256(str.encode(domain)).digest(),
            hashlib.sha256(str.encode(key)).digest(),
            iterations,
            PBKDF2_KEY_SIZE)

    return saltBuf

def get_seed_SHA():
    """Function to obtain a seed password, which will be 'salted'"""

    return hashlib.sha256(str.encode(getpass.getpass("Seed: ")))

def get_hash(seedSHA, saltBuf, iterations, noSpecialChars):
    """Function to obtain a url-safe base64-encoded key-stretched hash of
    the seed and the salt"""

    if not iterations:
        iterations = DEFAULT_ITERATIONS

    # (str.encode("foo") is the same as b"foo")
    hashObj = hashlib.pbkdf2_hmac(
            'sha256',
            seedSHA.digest(),
            saltBuf,
            iterations,
            PBKDF2_KEY_SIZE)

    hashStr = base64.b64encode(hashObj).decode("utf-8") if noSpecialChars \
                else zmq.utils.z85.encode(hashObj).decode("utf-8")

    return hashStr

def get_passwd_str(hashStr, truncation, noSpecialChars):
    """Function to convert the encoded hash string to
    a password string truncated to a desired length
    specified by 'truncation' if supplied."""

    # In the old avatar of gobbledygook, for base64 encoding,
    # the final password string would have been made "urlsafe"
    # as per RFC 4648: (+, /) -> (-, _).
    # Now, when special characters are allowed, Z85 is the preferred encoding
    # anyway, and therefore base64 is used in "strict mode", i.e.,
    # occurrences of (+,/,=) are simply deleted.
    # Therefore, base64.urlsafe_b64encode() is not necessary anymore.
    # For Z85 encoding, "/" is still replaced by "_"
    # (which is not part of the Z85 alphabet) for filename safety.
    # The resulting ASCII byte-array is converted to utf-8.
    passwdStr = hashStr.rstrip('=').replace("+", "").replace("/", "") \
            if noSpecialChars else hashStr.replace("/", "_")
    # Check if the output needs to be truncated
    if truncation is not None:
        print("Truncating to {0} characters...".format(truncation))
        passwdStr = passwdStr[0: truncation]

    return passwdStr

# =========================================================================
# MAIN

def main():
    """main"""

    # Parse commandline options
    parser = argparse.ArgumentParser(description='Obtain a salted password')
    parser.add_argument("-d", "--domain",
            help="the domain")
    parser.add_argument("-k", "--key",
            help="the 'key' used to generate a salt from the domain name")
    parser.add_argument("-n", "--iterations", type=int,
            default=DEFAULT_ITERATIONS,
            help="the number of iterations " +
            "(default: " + str(DEFAULT_ITERATIONS) + ")")
    parser.add_argument("-t", "--truncation", type=int,
            help="the # of truncated characters")
    parser.add_argument("-x", "--no-special",
            action="store_true",
            help="whether to not use special characters")

    args = parser.parse_args()
    print("args={0}".format(str(args)))

    # Obtain the "seed" SHA
    seedSHA = get_seed_SHA()
    print("seedSHA=[ {0} ]".format(seedSHA.hexdigest()))

    # Generate the salt from the domain name and the key
    saltBuf = generate_salt(args.domain, args.key, args.iterations)
    print("salt=[ {0} ]".format(base64.b64encode(saltBuf).decode("utf-8")))

    # Obtain the raw key-stretched hash
    hashStr = get_hash(seedSHA, saltBuf, args.iterations, args.no_special)
    print("hash=[ {0} ]".format(hashStr))

    # Obtain the final password string
    passwdStr = get_passwd_str(hashStr, args.truncation, args.no_special)
    print("password=[ {0} ], len={1}".format(passwdStr, len(passwdStr)))

if __name__ == "__main__":
    main()
