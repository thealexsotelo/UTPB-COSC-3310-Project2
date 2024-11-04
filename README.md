# UTPB-COSC-3310-Project2
This repo contains the assignment and provided code base for Project 2 of the Digital Computer Organization class.

The goals of this project are:
1) Gain experience and understanding of x86/64 assembly, and therefore also the inner workings of computers.

Description:
Using the provided x64 interactive interpreter, code the following program in our (extremely limited) version of x64:

Your compiled program should output whether the integer values from 1 to 99 are prime, in ascending order.  The output should be exactly
1 is not prime.
2 is prime.
3 is prime.
4 is not prime.
5 is prime.
etc.

When initially running the interpreter, it defaults to MODE 0, meaning that the full set of registers is available for you to use (this completely trivializes the project).

In order to get full credit on the project, your code must conform to the MODE 1 restrictions, meaning that only the A, B, C, and D registers are available to you, along with any pointer values you choose to create for yourself in the .data section.

For those of you who are truly bold, MODE 2 restrictions also make it so that all pointer values are constant.  This means that you will be required to make extensive use of the stack to handle shifting values around.  This is an extra credit opportunity for those who feel they need one.

For the grading, I will first attempt to copy your code to a real linux environment and compile/execute it there, to determine if it is valid x64 (I don't trust this interpreter, it's not very good).  If it fails and I can't make very easy changes to fix it, I will fall back on the interpreter output.

Grading criteria:
1) If the code submitted via your pull request does not compile within the interpreter, the grade is zero.
2) If the code experiences a segmentation fault in the interpreter, the grade is zero.
3) For full points, the code must correctly compile and run in a real environment.
4) If I have to fall back on the interpreter, I will take off a letter grade.

Deliverables:
One .asm file containing (hypothetically) valid x64 assembly code.