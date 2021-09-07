@echo off
echo My first name is %1
echo My surname is %2

set argC=0
for %%x in (%*) do Set /A argC+=1

echo Total number of arguments is %argC%