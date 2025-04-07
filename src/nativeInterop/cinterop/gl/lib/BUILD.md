# **GLAD static library for Windows**

Starting in [gl(ad) library directory](..):

The **libgladwin.a** static library for Windows was compiled using _clang_ (mingw-winlibs-llvm-ucrt) for Windows with:

```powershell
clang -c -Iinclude src/glad.c -o lib/glad.c.obj
```

Then on Linux (Ubuntu), using the _ar_ command line tool (since _lib.exe_ is not only impossible to get a hold of
without installing the 4+GBs of MSVC goodness, but is also a general piece of shit):

```bash
ar rcs lib/libgladwina.a lib/glad.c.obj
```
