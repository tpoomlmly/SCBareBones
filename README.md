An almost-working interpreter for languages following the Bare Bones definition.
Bare Bones has three simple commands for manipulating a variable:
```
clear name;
incr name;
decr name;
...which respectively sets variable name to zero, increments it by one and decrements it by one.
```

The language also contains one control sequence, a simple loop:
```
while name not 0 do;
...
...
end;
```
... where name is a variable. Note that variables need not be declared before they are used and must be non-negative integers. Statements are delimited by the ; character.

Note that while loops must be terminated by an end statement, but they can be nested.
