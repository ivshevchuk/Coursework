.386
.model flat, stdcall
option casemap :none

include D:\masm32\include\kernel32.inc
include D:\masm32\include\user32.inc

includelib D:\masm32\lib\kernel32.lib
includelib D:\masm32\lib\user32.lib

main PROTO

.data
msg_title db "Result", 0
buffer db 128 dup(?)
format db "%d",0

.code
start:
	invoke main
	invoke wsprintf, addr buffer, addr format, eax
	invoke MessageBox, 0, addr buffer, addr msg_title, 0
	invoke ExitProcess, 0

main proc
	push ebp
	mov ebp, esp
	push 2
	pop [ebp-12];	a_var
	push 2
	pop [ebp-16];	d_var
	push 10
	pop [ebp-20];	n_var
	push [ebp-16]     ;d_val
pop eax	;if
cmp eax, 0
jne _L2

	push [ebp-12]     ;a_val
	push [ebp-16]     ;d_val
	pop EAX
	pop EBX
	add EAX, EBX
	push EAX
	pop [ebp-12];	a_var
	jmp _L3
_L2:
	push 2
	pop [ebp-24];	i_var
_for_start1:
	mov ebx, [ebp-24]
	sub ebx, 1
	push [ebp-20]     ;n_val
	pop eax	;expression
	cmp eax, ebx
	jle _for_end1	;if value less than expr

	push [ebp-12]     ;a_val
	push [ebp-16]     ;d_val
	pop EAX
	pop EBX
	add EAX, EBX
	push EAX
	pop [ebp-12];	a_var
	push [ebp-24]     ;i_val
	push 1
	pop EAX
	pop EBX
	add EAX, EBX
	push EAX
	pop [ebp-24];	i_var
	jmp _for_start1
_for_end1:
_L3:
	push [ebp-12]     ;a_val
	pop eax ;here is the result
	mov esp, ebp	;restore ESP
	pop ebp	;restore old EBP
	ret 0

main ENDP
END start
