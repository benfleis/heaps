package minheap

import (
	"fmt"
	"math/rand"
	"sort"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestInsert(t *testing.T) {
	tests := []struct {
		pre   []uint
		value uint
		post  []uint
	}{
		{h(), 0, h(0)},
		{h(0), 1, h(0, 1)},
		{h(1), 0, h(0, 1)},
		{h(1), 0, h(0, 1)},
		{h(0, 1, 2, 3, 4, 5), 1, h(0, 1, 1, 3, 4, 5, 2)},
	}

	for _, test := range tests {
		// test the test
		require.NotNil(t, test.pre)
		require.NotNil(t, test.post)
		// and test insert
		t.Run(fmt.Sprintf("insert(%v, %v)", test.pre, test.value), func(t *testing.T) {
			actual := insert(test.pre, test.value)
			assert.Equal(t, test.post, actual)
		})
	}
}

func TestExtract(t *testing.T) {
	tests := [][]uint{
		h(0),
		h(0, 1),
		h(0, 1, 2),
		h(0, 2, 1),
		h(0, 1, 1, 3, 4, 5, 2),
	}

	for _, test := range tests {
		// test the test
		require.NotNil(t, test)
		// test extract: copy the heap into an array, and sort it, then pop
		// until done, checking along the way
		heap := test
		sorted := append([]uint(nil), test...)
		sort.Sort(UIntSlice(sorted))
		for _, value := range sorted {
			t.Run(fmt.Sprintf("extract(%v)", heap), func(t *testing.T) {
				var res uint
				heap, res = extract(heap)
				assert.Equal(t, value, res)
				assert.True(t, isHeap(heap))
			})
		}
		assert.Empty(t, heap)
	}
}

// TestSequences tests sequences of inserts and extracts. Each sequence has a
// target average heap size (=N), with a pre-defined number of iterations around
// that size. Each iteration consists of a up to N*2 consecutive inserts or
// extracts.
func TestSequences(t *testing.T) {
	r := rand.New(rand.NewSource(42))
	curAvgSize := 4
	const maxAvgSize = 64
	const itersPerSize = 16384

	for ; curAvgSize <= maxAvgSize; curAvgSize++ {
		t.Run(fmt.Sprintf("testSequence(iters=%v,size=%v)", itersPerSize, curAvgSize), func(t *testing.T) {
			testSequence(t, r, itersPerSize, curAvgSize)
		})
	}
}

func testSequence(t *testing.T, r *rand.Rand, iters, avgSize int) {
	heap := h()
	for iter := 0; iter < iters; iter++ {
		// calculate new target size from [0, avgSize]
		rnd := r.NormFloat64() / 3.0                   // 3 stddevs -> [-1, 1]
		tgtSize := int((rnd + 1.0) * float64(avgSize)) // scale to [0, 2 * avgSize]
		if tgtSize < 0 {
			tgtSize = 0
		} else if tgtSize > (2 * avgSize) {
			tgtSize = (2 * avgSize)
		}

		t.Run(fmt.Sprintf("toSize(%v)", tgtSize), func(t *testing.T) {
			for len(heap) < tgtSize {
				rheap := insert(heap, uint(rand.Int63()))
				assert.Len(t, rheap, len(heap)+1)
				assert.True(t, isHeap(rheap))
				heap = rheap
			}

			lastValue := uint(0)
			for len(heap) > tgtSize {
				rheap, value := extract(heap)
				assert.True(t, value >= lastValue, "value increased")
				assert.Len(t, rheap, len(heap)-1)
				assert.True(t, isHeap(rheap))
				lastValue = value
				heap = rheap
			}
		})
	}
}

// UIntSlice used to sort uint slices for extract tests
type UIntSlice []uint

func (a UIntSlice) Len() int           { return len(a) }
func (a UIntSlice) Swap(i, j int)      { a[i], a[j] = a[j], a[i] }
func (a UIntSlice) Less(i, j int) bool { return a[i] < a[j] }

// return args as heap; if args are not heap, returns nil
func h(values ...uint) []uint {
	// empty is ok, but not nil
	if values == nil {
		return []uint{}
	}

	// test heap property here
	for i, value := range values {
		if value < values[parentIndex(i)] {
			return nil
		}
	}

	return values
}

// h() func above, but as bool test instead of actual array
func isHeap(h_ []uint) bool {
	return h(h_...) != nil
}
