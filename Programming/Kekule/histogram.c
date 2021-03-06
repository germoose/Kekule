/* histogram.c, Wim H. Hesselink, June 2011 */
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "auxil.h"
#include "cells.h"

#define RANKLIM 11

static int rank = -1;

void setRank(int r) {
	rank = r;
	assert(rank <= RANKLIM);
}

void portHisto(Intstack cell, int *hist) {
	int i, j, k, w;
	for (i = 0; i < rank; i++)
		hist[i] = 0;
	for (j = 0; j < cell->size; j++) {
		k = cell->it[j];
		/* if rank = 5, w:= 1 gives 103 classes,
		 * w:= cntBits(k) gives  38 classes, 
		 * w:= cntBits(k) + 1 gives  32 classes */
		w = cntBits(k)+1;
		i = 0;
		while (k > 0) {
			assert(i < rank);
			if (k % 2)
				hist[i] += w;
			k /= 2;
			i++;
		}
	}
}

int portHistDescending(Intstack cell) {
	/* Is the weighted port histogram descending? */
	int i, k, m, hist[RANKLIM];
	assert(rank >= 0);
	assert(rank <= RANKLIM);
	portHisto(cell, hist);
	k = hist[0];
	for (i = 1; i < rank; i++) {
		m = hist[i];
		if (k < m)
			return 0;
		k = m;
	}
	return 1;
}

/* Given a cell, the weight of a port is the sum of sizes-1 
 * of the PAs it occurs in; the weight of a PA is the sum of 
 * the weights of its ports.
 * For computation, cell need not be ordered. */
void weightlist(Intstack cell) {
	/* Reuses the cell! */
	int i, p, x, we[RANKLIM];
	for (i = 0; i < RANKLIM; i++) {
		we[i] = 0;
	}
	assert(rank >= 0);
	for (i = 0; i < cell->size; i++) {
		x = cntBits(cell->it[i]) - 1;
		for (p = 0; p < rank; p++) {
			if ((cell->it[i] >> p) & 1)
				we[p] += x;
		}
	}
	
	for (i = 0; i < cell->size; i++) {
		x = cell->it[i];
		cell->it[i] = 0;
		for (p = 0; p < rank; p++) {
			if ((x >> p) & 1)
				cell->it[i] += we[p];
		}
	}
	qusort(cell->it, 0, cell->size);
}

int compareLex(int r, const int *h1, const int *h2) {
	r--;
	while (r >= 0 && h1[r] == h2[r]){
		r--;
	}
	return (r < 0 ? 0 : h1[r] - h2[r]);
}

int compareL(const Intstack ce1, const Intstack ce2) {
	int r = ce1->size - ce2->size;
	if (r != 0)
		return r;
	
	return compareLex(ce1->size, ce1->it, ce2->it);
	
}

int compareW(const Intstack ce1, const Intstack ce2) {
	int r;
	Intstack h1 = newIntstack(0, ce1), h2 = newIntstack(0, ce2);
	weightlist(h1);
	weightlist(h2);
	r = compareL(h1, h2);
	freestack(h1);
	freestack(h2);
	return r;
}

int compareCell(const Intstack ce1, const Intstack ce2) {
	int r = compareW(ce1, ce2);
	if (r != 0)
		return r;
	return compareL(ce1, ce2);
}

int isCentered(Intstack cell, long int x) {
	Intstack owl, other;
	int i, j, r = 0;
	if (cell->size == 0 || cell->it[0] > 0){
		return 0;
	}
	owl = newIntstack(0, cell);
	other = newIntstack(cell->size, NULL);
	weightlist(owl);

	other->size = cell->size;
	for (i = 1; r <= 0 && i < cell->size; i++) {
		for (j = 0; j < cell->size; j++) {
			other->it[j] = cell->it[i] ^ cell->it[j];
		}
		weightlist(other);
		r = compareL(owl, other);
	}
	freestack(other);
	freestack(owl);
	return r <= 0;
}

void toCenter(Intstack cell, int *pa) {
	/* Pre: cell is nonempty.
	 * Translate cell such that it becomes centered */
	int i, k = 0;
	Intstack wl, owl;
	assert(cell->size> 0);
	wl = newIntstack(0, cell);
	translate(cell->it[0], wl);
	weightlist(wl);
	
	for (i = 1; i < cell->size; i++) {
		owl = newIntstack(0, cell);
		translate(cell->it[i], owl);
		weightlist(owl);

		if (compareL(owl, wl) < 0) {
			k = i;
			freestack(wl);
			wl = owl;
		} else {
			freestack(owl);
		}
		
	}
	
	freestack(wl);
	*pa = cell->it[k];
	translate(cell->it[k], cell);
}

Intstack centers(Intstack cell) {
	/* Pre: cell is centered */
	int i, j, r;
	Intstack result = newIntstack(0, NULL), owl = newIntstack(0, cell) , other = newIntstack(cell->size, NULL);
	other->size = cell->size;
	weightlist(owl);
	putint(0, result);
	for (i = 1; i < cell->size; i++) {
		for (j = 0; j < cell->size; j++)
			other->it[j] = cell->it[i] ^ cell->it[j];
		
		weightlist(other);
		r = compareL(owl, other);
		assert(r <= 0);
		if (r == 0)
			putint(cell->it[i], result);
	}
	freestack(owl);
	freestack(other);
	return result;
}

void printHisto(Intstack cell) {
	int i, hist[RANKLIM];
	setRank(twolog(bitUnion(cell)));
	portHisto(cell, hist);
	for (i = 0; i < rank; i++) {
		printf(" %2d", hist[i]);
	}
}

void printWeightlist(Intstack cell) {
	Intstack cp = newIntstack(0, cell);
	weightlist(cp);
	printar(cp);
	freestack(cp);
}
