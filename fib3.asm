; This file contains a version of the fibonacci code which uses recursion, and is formatted to work within a real linux compiler (with QWORD)

section .data:
newline db 0x0a, 0x00
nl_len equ $ - newline
nums db '0123456789'
iter dq 15

section .text
global _start

_start:
        push 1
        push 1
        push QWORD [iter]
        call fn_fib

        mov rax, 1
        int 0x80


fn_fib:
	pop rdx
        pop rcx
        pop rbx
        pop rax
        push rdx

        cmp rcx, 0
        jle fn_exit

        push rax
        push rbx
        push rcx
        push rax
        call fn_print_number

        pop rcx
        pop rbx
        pop rax
        sub rcx, 1
        add rax, rbx
        push rbx
        push rax
        push rcx
        call fn_fib

        fn_exit:
        ret

fn_print_nl:
	mov rdx, nl_len
	mov rcx, newline
	mov rbx, 1
	mov rax, 4
	int 0x80
	ret

fn_print_digit:
	pop rax
	pop rdx
	push rax
	mov rcx, nums
	add rcx, rdx
	mov rdx, 1
	mov rbx, 1
	mov rax, 4
	int 0x80
	ret

fn_print_number:
	pop rax
	pop rdx
	push rax
	cmp rdx, 10
	push rdx
	jge decomp
	call fn_print_digit

	call fn_print_nl
	ret
	decomp:
		decomp_loop_init:
			mov r13, 0
			pop r11
		decomp_loop_head:
			cmp r11, 0
			je decomp_loop_exit
		decomp_loop_body:
			mov rdx, 0
			mov rcx, 0
			mov rbx, 10
			mov rax, r11
			div rbx
			mov r11, rax
			push rdx
		decomp_loop_tail:
			add r13, 1
			jmp decomp_loop_head
		decomp_loop_exit:
			decomp_print_loop_init:
			decomp_print_loop_head:
				cmp r13, 0
				je decomp_print_loop_exit
			decomp_print_loop_body:
				call fn_print_digit
			decomp_print_loop_tail:
				sub r13, 1
				jmp decomp_print_loop_head
			decomp_print_loop_exit:
				call fn_print_nl
				ret
