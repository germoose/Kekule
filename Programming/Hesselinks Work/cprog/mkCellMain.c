
/* mkCellMain.c, Wim H. Hesselink, June 2011 */
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include "auxil.h"
#include "arraylist.h"
#include "graph.h"
#include "cells.h"
#include "readKekule.h"
#include "scanner.h"
#include "permutations.h"
#include "histogram.h" 
#include "mkCell.h"

void normalize(int rank, int nr, Intstack cell) {
  int pa;
  Intstack nc = newIntstack(0, cell) ;
  printf("Cell = "); 
  printCell(-1, cell) ;
  toCenter(nc, &pa) ; /* see histogram.c */
  printf("translated over '");
  printPA(pa);
  printf("' and normalized, gives:\n");
#if 1
  nc = firstVariant(rank, nc) ; /* see permutations.c */
#endif
  printCell(0, nc) ;
  freestack(nc) ;
}

int main(int argc, char *argv[]) {
  Graph g;
  Intstack a;
  int nr = 0;
  printf("From mkCell.c.\n");
  g = readGraph();
  while(g){
    setRank(g->cP);
    writeGraph(g);
    a = kpa(g);
#if 0
    printCell(++nr, a); 
    if (isCentered(a)){
      Intstack a1;
      printf("Centers: ");
      a1 = centers(a);
      printCell(-1 , a1);
      freestack(a1);
    } else printf("Not centered.\n");
#endif
    normalize(g->cP, 0, a);
    printf("\n");
    freestack(a);
    freeGraph(g);
    g = readGraph();
  }
  finalizeScanner();
  freePerm();
#if 1
  reportCnt();
  reportACnt();
  reportGCnt();
#endif
  return 0;
}
