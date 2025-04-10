# GLAD static library for Windows

Starting in [gl(ad) library directory](cinterop/gl):

The **libgladwin.a** static library for Windows was compiled using _clang_ (mingw-winlibs-llvm-ucrt) for Windows with:

```powershell
clang -c -Iinclude src/glad.c -o lib/glad.c.obj
```

Then on Linux (Ubuntu), using the _ar_ command line tool (since _lib.exe_ is not only impossible to get a hold of
without installing the 4+GBs of MSVC goodness, but is also a general piece of shit):

```bash
ar rcs lib/libgladwina.a lib/glad.c.obj
```

# GLFW static library for Linux

Assuming in the root directory of a GLFW repo or untarred source artifact:

Create the build configuration for X11 (as Wayland interfaces with libs which are not natively available on some distros):
```bash
cmake -S . -B build -DGLFW_BUILD_WAYLAND=0 -DGLFW_BUILD_X11=1 -D
```
Then run the build for the `glfw` target:
```bash
cmake --build build -- glfw
```
The static library is then found in _build/src/libglfw3.a_.
