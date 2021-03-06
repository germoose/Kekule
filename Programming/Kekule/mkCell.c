/* mkCell.c, Wim H. Hesselink, June 2011 */
#include <stdlib.h>
#include <stdio.h>
#include "auxil.h"
#include "graph.h"
#include "cells.h"

static int ports;

/* Function M(U) of whh424, section 4.2.
 * Computed according to Lemma 3.
 * The set of nodes U is represented by the bit vector uu.
 * The set of ports is represented by the bit vector ports. 
 * 
 * A cell is a set of port assignments; 
 * the port assignments are represented as bit vectors (integers); 
 * a set of them is represented as an ordered intstack.
 */
Intstack mkcell(int uu, Graph g) {
		
	int u, i, v;
	//only ed set to edges
	//rest are null
	Intstack acc, addend, ed = g->edges;
	
	ports = (1 << g->cP) - 1;
	
	//Base case
	if (uu == 0) {
		acc = newIntstack(5, NULL);
		putint(0, acc);
	} else {
		//find first occurence of vertex
		u = firstspike(uu);
		//remove vertex from set
		uu = u ^ uu;
		//recurse
		acc = (u & ports ? mkcell(uu, g) : newIntstack(5, NULL));
		
		/* treat nbh(u, g) */
		for (i = 0; i < ed->size; i++)
			//if u in edge[i]
			if (u & ed->it[i]) {
				//get another vertex
				v = firstspike(u ^ ed->it[i]);
				//if v in uu
				if (v & uu) {
					//remove v from uu, and get cell
					addend = mkcell(uu ^ v, g);
					//translate cell (addend) over port assignment (ports & (u+v) )
					translate(ports & (u+v), addend);
					//acc = acc union addend
					//addend = null
					intunion(acc, addend);
				}
			}
	}
	return acc;
}

/* Kp(G) */
Intstack kpa(Graph g) {
	return mkcell((1 << g->cN) - 1, g);
}

void minimizeGraph(Graph g) {
	Intstack cell, acell, ed;
	int i = 0, edge;
	ed = g->edges;
	if (ed->size == 0)
		return;
	cell = kpa(g);
	ed->size--;
	while (i <= ed->size) {
		edge = ed->it[i];
		ed->it[i] = ed->it[ed->size];
		acell = kpa(g);
		if (subset(cell, acell)) {
			printf("THIS NEVER HAPPENS");
			printf("  Removing edge.\n");
			freestack(cell);
			cell = acell;
			ed->size--;
		} else {
			ed->it[i] = edge;
			freestack(acell);
			i++;
		}
	}
	ed->size++;
	freestack(cell);
}
