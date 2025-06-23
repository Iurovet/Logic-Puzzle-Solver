import { useState, useEffect } from 'react';
import './App.css';

function App() {
  // Size is given by 34, 35, 44, 45, 46, 47.
  // The 1st digit indicates the number of interconnected subgrids, the 2nd their dimensions
  const [gridSize, setGridSize] = useState(34)
  const [stepSize, setStepSize] = useState(1);
  const [entityNames, setEntityNames] = useState(new Array(Math.floor(gridSize / 10)).fill(new Array(gridSize%10).fill("1")));

  const handleGridSizeChange = (event) => {
    setGridSize(event.target.value);
    setEntityNames(new Array(Math.floor(gridSize / 10)).fill(new Array(gridSize%10).fill("1")));
  }
  const handleStepSizeChange = (event) => { setStepSize(event.target.value); }
  const handleNameChange = (rowIndex, colIndex, newValue) => {
    setEntityNames(entityNames => {
      const newGridData = [...entityNames]; // Shallow copy of outer array
      const newRow = [...newGridData[rowIndex]]; // Shallow copy of inner array
      newRow[colIndex] = newValue;
      newGridData[rowIndex] = newRow;
      return newGridData;
    });
  };

  useEffect(() => {
    const newGridData = [...entityNames]; // Shallow copy of outer array
    const newRow = [...newGridData[0]]; // Shallow copy of inner array
    
    for (let i = 1; i < gridSize % 10; ++i) {newRow[i] = Number(newRow[i-1]) + Number(stepSize);}
    newGridData[0] = newRow;

    if (entityNames !== newGridData) {
      setEntityNames(newGridData);
    }
  }, [entityNames[0][0], gridSize, stepSize]);
  
  const handleSubmit = (event) => {
    event.preventDefault();
  }

  function getEntityNameSlotOne() {
    let entityNameSlotOne = []
    entityNameSlotOne.push(
      <td>
        <label htmlFor="entityName"></label>
        <input name="category1" id={"category1 start"} type="number" min="1" step="1" placeholder="Enter a starting positive integer here"
        value={entityNames[0][0] || 1} onChange={(e) => handleNameChange(0, 0, e.target.value)}></input>
      </td>)

    for (let i = 1; i < gridSize % 10; ++i) {
      entityNameSlotOne.push(<td>{entityNames[0][i] || 1}</td>)
    }

    entityNameSlotOne.push(
      <td>
        <label htmlFor="entityName"></label>
        <input name="category1" id={"category1 step"} type="number" min="1" step="1" placeholder="Enter a step size here"
        value={stepSize || 1} onChange={handleStepSizeChange}></input>
      </td>)

    return entityNameSlotOne
  }

  // function getEntityNameSlots(rowNum) {
  //   let entityNameSlots = []
  //   for (let i = 0; i < numCols; ++i) {
  //     entityNameSlots.push(
  //     <td>
  //       <label htmlFor="entityName">Test</label>
  //       <input name="props.name" id="props.name" value={entityNames[rowNum][i] || ''} onChange={(e) => handleNameChange(rowNum, i, e.target.value)}></input>
  //     </td>)
  //   }

  //   return entityNameSlots
  // }

  function testTable() {
    let entityNameNumbers = []
    for (let i = 0; i < gridSize % 10; ++i) {
      entityNameNumbers.push(<td>{i + 1}</td>)
    }

    let categoryNameSlots = []
    categoryNameSlots.push(<tr><td>1</td>{getEntityNameSlotOne()}</tr>)
    // for (let i = 1; i < numRows; ++i) {
    //   categoryNameSlots.push(<tr><td>{i + 1}</td>{getEntityNameSlots(i)}</tr>)
    // }

    return (
      <>
        <table>
          <tr><td>Category number/entity name</td>{entityNameNumbers}<td>Increment for Category 1</td></tr>
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
          <select name="gridSize" id="gridSize" onChange={handleGridSizeChange}>
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
        <tr><td>TRUE/FALSE</td><td>2</td><td>Items 1 and 2 form a true/false relationship</td></tr>
        <tr><td>DIFF</td><td>X</td><td>Items 1 thru X correspond to different entities</td></tr>
        <tr><td>(N)EITHER</td><td>3</td><td>Item 1 corresponds to (n)either of Items 2 and 3</td></tr>
        <tr><td>LESS/MORE</td><td>2</td><td>Item 1 is less/more than Item 2 (using Category 1) A1 is an optional parameter to specify the distance</td></tr>
        <tr><td>(NOT)EQUALS</td><td>2</td><td>Item 1 forms a (false)true relationship with item 2 (the latter being from category 1)</td></tr>
        <tr><td>ALIGN</td><td>4</td><td>Of Items 1 and 2, one corresponds to Item 3, the other to Item 4</td></tr>
      </table>
    </div>
  );
}

export default App;
