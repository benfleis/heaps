package minheap

func insert(heap []uint, value uint) []uint {
	rv := append(heap, value)
	cur := len(rv) - 1
	par := parentIndex(cur)

	// fmt.Printf("heap=%v\n", rv)
	for ; cur > 0 && rv[par] > rv[cur]; cur, par = par, parentIndex(par) {
		// fmt.Printf("cur/@=%v/%v par/@=%v/%v\n", cur, par, rv[cur], rv[par])
		rv[cur], rv[par] = rv[par], rv[cur]
	}
	return rv
}

func parentIndex(index int) int {
	return (index - 1) / 2
}

func extract(heap []uint) ([]uint, uint) {
	// return top element
	rv := heap[0]

	// swap first/last
	heap[0] = heap[len(heap)-1]
	rheap := heap[:len(heap)-1]

	// bubble down
	cur := 0
	for cur < len(rheap) {
		left, right := childIndices(cur)
		if right < len(rheap) && rheap[right] < rheap[cur] && rheap[right] < rheap[left] {
			rheap[right], rheap[cur] = rheap[cur], rheap[right]
			cur = right
			continue
		}

		if left < len(rheap) && rheap[left] < rheap[cur] {
			rheap[left], rheap[cur] = rheap[cur], rheap[left]
			cur = left
			continue
		}

		break
	}

	return rheap, rv
}

func childIndices(index int) (left, right int) {
	return index*2 + 1, index*2 + 2
}
