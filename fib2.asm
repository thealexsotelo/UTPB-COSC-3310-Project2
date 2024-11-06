section .data
newline db 0x0a, 0x00
nl_len equ $ - newline
nums db '0123456789'
num0 dq 1
num1 dq 1
num2 dq 1
count dq 2
iter dq 10

section .text
global _start

_start:

init:
mov rax, 1
push rax
push rax
call fn_print_number
call fn_print_number

loop_head:
mov r10, [iter]
cmp [count], r10
jge loop_exit

loop_body:
mov r10, [num0]
mov [num2], r10
mov r10, [num1]
add [num0], r10
mov r10, [num2]
mov [num1], r10

push [num0]
call fn_print_number

loop_tail:
add [count], 1
jmp loop_head

loop_exit:
mov rax, 1
int 0x80

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
	
	mov rdx, nl_len
	mov rcx, newline
	mov rbx, 1
	mov rax, 4
	int 0x80
	ret

fn_print_number:
	pop rax
	pop rdx
	push rax
	cmp rdx, 10
	jge decomp
	push rdx
	call fn_print_digit
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
				ret