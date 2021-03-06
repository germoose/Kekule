/* readKekule.c, Wim H. Hesselink, August 2010 */
/* Read functions */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "auxil.h"
#include "arraylist.h"
#include "scanner.h"
#include "graph.h"

#define INLIMIT 400

static int cnr = 17;

int getCnr() { /* most recent cell number */
	return cnr;
}

Intstack readCell() {
	Intstack a;
	readLine(stdin, 1);
	if (accept('[')) {
		if (getChar() == '0') {
			cnr = getNumber();
		}
		do {
			scan();
		} while (!accept(']'));
	}
	if (getChar() != '0')
		return NULL;
	a = newIntstack(5, NULL);
	while (getChar() == '0') {
		putint(getNumber(), a);
		scan();
	}
	if (!accept('.'))
		assert(0);
	return a;
}

Arraylist readCellList() {
	Intstack cell;
	Arraylist ar = newArraylist(5);
	do {
		cell = readCell();
		if (cell)
			putItem(cell, ar);
	} while (cell);
	return ar;
}

Intstack mkPortPermutation(int cN, Intstack b) {
	Intstack c = newIntstack(cN, NULL);
	int i, k;
	for (i = 0; i < cN; i++) {
		putint(-1, c);
	}
	for (i = 0; i < b->size; i++) {
		c->it[b->it[i]] = i;
	}
	k = b->size;
	for (i = 0; i < cN; i++) {
		if (c->it[i] < 0) {
			c->it[i] = k;
			k++;
		}
	}
	assert(k == cN);
	return c;
}

int apply(Intstack c, int x) {
	return (c ? c->it[x] : x);
}

/* Syntax of graphs
 * First one or more identifiers that serve as name and comment
 * The two numbers cP (#ports) and cN (#nodes)
 * semicolon
 * list like 1-2-3, 2-5-3.
 * The number cP can be replaced by {a b c} of named ports < cN
 */

Graph readGraph() {
	Intstack a, b= NULL, c= NULL;
	int cP = 0, cN = 0, k, n;
	readLine(stdin, 0);
	if (getChar() != 'a')
		return NULL;
	a = newIntstack(5, NULL);
	while (getChar() == 'a') {
		printf("%s ", getIdent());
		scan();
	}
	if (accept('{')) {
		b = newIntstack(8, NULL);
		while (getChar() == '0') {
			k = getNumber();
			putint(k, b);
			cP ++;
			scan();
		}
		if (!accept('}'))
			assert(0);
	} else {
		assert(getChar() == '0');
		cP = getNumber();
		scan();
	}
	assert(getChar() == '0');
	cN = getNumber();
	assert(0 <= cP && cP <= cN);
	scan();
	if (!accept(';'))
		assert(0);
	if (b) {
		c = mkPortPermutation(cN, b);
	}
	do {
		assert(getChar() == '0');
		k = apply(c, getNumber());
		scan();
		while (accept('-')) {
			assert(getChar() == '0');
			n = apply(c, getNumber());
			scan();
			putint((1<<k) + (1<<n), a);
			k = n;
		}
	} while (accept(','));
	if (!accept('.'))
		assert(0);
#if 0
	qusort(a->it, 0, a->size);
	printarn(a);
#endif
	freestack(b);
	freestack(c);
	return newGraph(cP, cN, a);
}
