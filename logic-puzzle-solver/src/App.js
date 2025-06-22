import { useState } from 'react';
import './App.css';

function App() {
  // Size is given by 34, 35, 44, 45, 46, 47.
  // The 1st digit indicates the number of interconnected subgrids, the 2nd their dimensions
  const [gridSize, setGridSize] = useState(34)
  const handleSizeChange = (event) => {
    setGridSize(event.target.value)
  }

  const [entityNames, setEntityNames] = useState([[[""], [""], [""], [""]],
                                                  [[""], [""], [""], [""]],
                                                  [[""], [""], [""], [""]]])
  
  const handleSubmit = (event) => {
    event.preventDefault();
  }

  function testTable() {
    const numRows = (gridSize - gridSize % 10) / 10
    const numCols = gridSize % 10

    let entityNameNumbers = []
    for (let i = 0; i < numCols; ++i) {
      entityNameNumbers.push(<td>{i + 1}</td>)
    }

    let entityNameSlots = []
    for (let i = 0; i < numCols; ++i) {
      entityNameSlots.push(<td>Test</td>)
    }

    let categoryNameSlots = []
    for (let i = 0; i < numRows; ++i) {
      categoryNameSlots.push(<tr><td>{i + 1}</td>{entityNameSlots}</tr>)
    }

    return (
      <>
        <table>
          <tr>
            <td>Category number/entity name</td>
            {entityNameNumbers}
            <td>Increment for Category 1</td>
          </tr>
          {categoryNameSlots}
        </table>
        *Note: We assume that category 1 has an integer for the 1st name, which increases by an integer for the subsequent amounts
      </>
    );
  }

  return (
    <div className="App">
      <form onSubmit={handleSubmit}>
        <div>
          <label for="gridSize">Please select the size of your grid</label>
          <select name="gridSize" id="gridSize" onChange={handleSizeChange}>
            <option value="34">3x4</option>
            <option value="35">3x5</option>
            <option value="44">4x4</option>
            <option value="45">4x5</option>
            <option value="46">4x6</option>
            <option value="47">4x7</option>
          </select>
        </div>
        
        {testTable()}
        {/* <button type='submit' class='btn btn-primary'>Build</button> */}
      </form>

      <br/>
      
      <table>
        <tr><td>Keyword (case-insensitive):</td><td>Number of items</td><td>Details</td></tr>
        <tr><td>TRUE/FALSE</td><td>2</td><td>Items 1 and 2 form a true/false relationship.</td></tr>
        <tr><td>DIFF</td><td>X</td><td>Items 1 thru X correspond to different entities.</td></tr>
        <tr><td>(N)EITHER</td><td>3</td><td>Item 1 corresponds to (n)either of Items 2 and 3.</td></tr>
        <tr><td>LESS/MORE</td><td>2</td><td>Item 1 is less/more than Item 2 (using Category 1). A1* is an optional parameter to specify the distance.</td></tr>
        <tr><td>(NOT)EQUALS</td><td>1</td><td>Item 1 forms a (false)true relationship with the value A1*</td></tr>
        <tr><td>ALIGN</td><td>4</td><td>Of Items 1 and 2, one corresponds to Item 3, the other to Item 4.</td></tr>
      </table>
      *If A1 is not a whole multiple of the increment up from the lower bound (but within range of the possible Category 1 values), it will be rounded down to the nearest of such multiples.
    </div>
  );
}

export default App;
