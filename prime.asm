; This file contains code which uses a rather large character array and some pointer manipulation to print out all of the numbers from 1 to 99 in order.
section .data:
newline db 0x0a, 0x00
nl_len equ $ - newline
nums db '123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899'

section .text
global _start

_start:
	loop_init:
		xor r10, r10

	loop_head:
		cmp r10, 189
		jge loop_exit

	loop_body:
		mov rcx, nums
		add rcx, r10
		mov rdx, 1
		cmp r10, 9
		jl print
		add rdx, 1
		print:
		mov rbx, 1
		mov rax, 4
		add r10, rdx
		int 0x80

		mov rdx, nl_len
		mov rcx, newline
		mov rbx, 1
		mov rax, 4
		int 0x80

		jmp loop_head
	loop_exit:
		mov rax, 1
		int 0x80
