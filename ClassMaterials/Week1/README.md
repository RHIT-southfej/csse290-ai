
# Week 1 Homework

Open some somewhat larger source code project in VisualStudioCode with
Github Copilot installed and working.  This could be a project of your
own or homework from some past course.  It's best if you can still run
the code.

## Step 1: Try the autocompletion

Go to some random file or class and begin writing a function with a
suggestive name.  Like this:

    public static King fromSavedFile(

Initially, it might be a bit conservative about what it might
generate.  See if you can get it to generate a large amount of code
for you, based on a short header name.

Include in your submission:

(a) the short function name that generated a lot of tetx for you
(b) the code it generated

## Step 2: Try a local command

On my visual studio code this is Ctrl+I.

Find a place in the codebase where you think a short function makes
sense - something a little more complex than a getter.  Issue a
command to attempt to get it to write that function for you, without
your input.  If it doesn't work initially, try and few times and see
if you can get to do it.

Try to break it.  Tell it to generate something that would require
editing a different part of the file (e.g. asking it add something to
a different function than you are currently in) or ask it to do
something that requires knowledge of a class outside the local file.

Include in your submission:

(a) The function code it generated for you
(b) The erronous code it generated for it, if you were able to trick it

## Step 3: Try a Agent Command

On my visual studio code this is Ctrl+Shift+I.

Think about a larger feature or refactoring that could be added to
this codebase, something that would require changes to multiple files
and you can easily check if it worked.  Write a detailed description
of the feature you want added, being sure to reference the files you
need changed as context + anything else it would need.

If it doesn't get it right at first, interact with it and see if you
can get it closer to a working implementation.

It's not required you get it working, but play with it a bit.