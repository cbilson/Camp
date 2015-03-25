# camp

Camp is a tool that makes it easy to use ClojureCLR.

	- Create a new project.
	- Fetch dependencies.
	- Compile it.
	- Run it.
	- Open a REPL on it.
	- Package and deploy to nuget.org.

## Camp Philosophy

Camp sets up a safe place for your stuff, so you can go off and
explore. It will have coffee ready for you when you wake up in the
morning and dinner ready when you get back from a day roaming the
hills around Mt. Clojure.

It tries to stay out of your business but be as helpful and safe as
possible.

## How to Use

*Red Alert: Pre-usable software ahead.*

Note: Camp is kind of windows-centric right now, mostly because I
haven't had time to investigate getting it to run under mono. Rest
assured that mono+other-OS support is high on the priority list.

Install via [chocolatey](https://chocolatey.org/) or get the source
and compile yourself (see "How to Hack" below.)

```shell
$ choco install -pre camp.portable
```

Once camp is installed,

```shell
$ camp new foo
$ cd foo
```

You can add nuget dependencies by adding them to the `project.clj`
file at the root of your project:

```clojure
(defproject foo "0.1.0-SNAPSHOT"
  :description "TODO: describe"
  :license {:name "BSD"
            :url "http://www.opensource.org/licenses/BSD-3-Clause"
            :distribution :repo}
  :dependencies [[Clojure "1.6.0.1"]
                 [NUnit "2.6.4"]
                 [Microsoft.Net.Http "2.2.28"]])
```

Note: the version specifier uses nuget's
[version range notation](http://docs.nuget.org/Create/Versioning), so
"1.6.0.1" actually means ">= 1.6.0.1". If you want to use exactly
"1.6.0.1", you need to surround it in brackets, so the dependency
spec would be like '[Clojure "[1.6.0.1]"]'.

There is also a rudimentary template for building an OWIN based, self-hosted web application:

```shell
$ camp new webapp my-webapp
```

Once the project is created and you are in the root directory, use `camp deps` to
fetch the dependencies.

```shell
$ camp deps
Installing [Clojure 1.6.0.1]
Installing [NUnit 2.6.4]
Installing [Microsoft.Net.Http 2.2.28]
```

The dependencies go in the packages folder, just as if you used a
`packages.config` with nuget.

```shell
$ ls packages

Mode                LastWriteTime         Length Name
----                -------------         ------ ----
d-----        2/20/2015   6:28 AM                Clojure.1.6.0.1
d-----        2/21/2015  10:46 AM                Microsoft.Bcl.1.1.9
d-----        2/21/2015  10:46 AM                Microsoft.Bcl.Build.1.0.14
d-----        2/21/2015  10:46 AM                Microsoft.Net.Http.2.2.28
d-----        2/21/2015  10:46 AM                NUnit.2.6.4
```

Now, write some code. There will be one source file, `src\core.clj`
which you can use as a starting point.

To use an assembly from a dependency you have added, add something
like:

```clojure
;;; Load with partial name
(assembly-load "Microsoft.Net.Http")

(ns my.ns
  (:import [System.Net.Http HttpClient HttpResponseMessage]))
```

When you are ready:

```shell
$ camp compile
```

In your project's root directory you will find `targets`, which will
contain all the assemblies needed to run your project: Clojure, all
your dependencies, and one assembly for each source file / namespace
in your project.

If you want to produce an executable, be sure to have a namespace in
your project that has `:gen-class` and a `-main` method, like

```clojure
(ns foo.core
  (:gen-class))

(defn -main [& args]
  (println "I don't do much yet."))
```

This will cause the compiler to generate an exe called `foo.core.exe`
in addition to a DLL for the `core` namespace and any other namespace
you have.

## How to Hack

Get the source, make changes, and

```shell
$ msbuild /t:CampExe
```

If you have [Chocolatey](https://chocolatey.org/) installed on your
system, you should be able to build and install the package, by saying

```shell
$ msbuild /t:ChocoInstall
```

Then `camp` will be in your path.

Otherwise you can always run it from the targets directory

```shell
$ camp\targets\camp.exe new mycampproj
```

## What about Leiningen?

Leiningen is a shining example of what a development tool should be
like. Unfortunately, Leiningen is so good that it's being used outside
of it's comfort zone. Leiningen is perfectly up to the task of
building ClojureCLR applications, but to me, feels a little alien when
used for non-JVM projects. It can't really take full advantage of the
capabilities of other platforms. I think we can do better than to just
try and make the same Leiningen do all this.

Additionally there is the problem of developer participation. A lot of
javascript and CLR developers are not interested in learning another
platform's ins-and-outs to contribute to a build tool.

Camp is not intended to be a "leiningen clone on the CLR", but I'd be
nothing less than flattered if someone made the mistake of thinking
that it was someday.

## Roadmap

See Notes.org for detailed plans and othe notes, but real quick:

### Does it work with Mono?

I don't know. Give it a try and let me know. I haven't had a chance to
try yet.

XBuild may be able to run the build script. If it doesn't we can fix
it.

I don't think anything else about clojure-clr or the dependencies camp
has would prevent it from running under mono, and I feel it's
important that it does work on Mono. A medium-term goal would be to
get ClojureCLR to work with Xamarin, so you could use it to build
native iOS, Android, and OSX apps.

### What about other types of projects? Templates?

Yes, that's high on my list. I plan to have console, library, and
webapp templates very soon.

### Can I use it from Cider in Emacs?

That's definately a major part of my plan. I haven't done any work to
use NREPL CLR yet, but plan to very soon.

### Can I use «my editor»?

I suppose, once NREPL support is done. But I pretty much just use
emacs, so I probably won't be doing any work on this. If you have to
change something to get it to work with another editor, please send a
pull request.

### Can I use Visual Studio?

I know there exists a Visual Studio plugin for clojure-clr, called
vsClojure, but I know next to nothing about it. It might be easy to
make camp and vsClojure work together, or it might not. I am not
sufficiently motivated to look at this point, but if there is
something I can do to help make it work, let me know.
