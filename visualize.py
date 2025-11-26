import pandas as pd
import matplotlib.pyplot as plt

# trend1-regime1
df = pd.read_csv('/content/sample_data/trend1-regime1.csv')
df_sorted = df.sort_values('k', ascending=True)

plt.plot(df['k'], df['pageFaultsOfOpt'], label = "pageFaultsOfOpt")
plt.plot(df['k'], df['pageFaultsOfBlindOracle'], label = "pageFaultsOfBlindOracle")
plt.plot(df['k'], df['pageFaultsOfLRU'], label = "pageFaultsOfLRU")
plt.plot(df['k'], df['pageFaultsOfCombined'], label = "pageFaultsOfCombined")

plt.title("Page faults of algorithms over varying cache size -- Regime 1")
plt.xlabel("cache size")
plt.ylabel("average page faults")

plt.legend()
plt.show()

# trend1-regime2
df = pd.read_csv('/content/sample_data/trend1-regime2.csv')
df_sorted = df.sort_values('k', ascending=True)

plt.plot(df['k'], df['pageFaultsOfOpt'], label = "pageFaultsOfOpt")
plt.plot(df['k'], df['pageFaultsOfBlindOracle'], label = "pageFaultsOfBlindOracle")
plt.plot(df['k'], df['pageFaultsOfLRU'], label = "pageFaultsOfLRU")
plt.plot(df['k'], df['pageFaultsOfCombined'], label = "pageFaultsOfCombined")

plt.title("Page faults of algorithms over varying cache size -- Regime 2")
plt.xlabel("cache size")
plt.ylabel("average page faults")

plt.legend()
plt.show()

# trend2-regime1
df = pd.read_csv('/content/sample_data/trend2-regime1.csv')
df_sorted = df.sort_values('omega', ascending=True)

plt.plot(df['omega'], df['pageFaultsOfOpt'], label = "pageFaultsOfOpt")
plt.plot(df['omega'], df['pageFaultsOfBlindOracle'], label = "pageFaultsOfBlindOracle")
plt.plot(df['omega'], df['pageFaultsOfLRU'], label = "pageFaultsOfLRU")
plt.plot(df['omega'], df['pageFaultsOfCombined'], label = "pageFaultsOfCombined")

plt.title("Page faults of algorithms over varying omega -- Regime 1")
plt.xlabel("omega")
plt.ylabel("average page faults")

plt.legend()
plt.show()

# trend2-regime2
df = pd.read_csv('/content/sample_data/trend2-regime2.csv')
df_sorted = df.sort_values('omega', ascending=True)

plt.plot(df['omega'], df['pageFaultsOfOpt'], label = "pageFaultsOfOpt")
plt.plot(df['omega'], df['pageFaultsOfBlindOracle'], label = "pageFaultsOfBlindOracle")
plt.plot(df['omega'], df['pageFaultsOfLRU'], label = "pageFaultsOfLRU")
plt.plot(df['omega'], df['pageFaultsOfCombined'], label = "pageFaultsOfCombined")

plt.title("Page faults of algorithms over varying omega -- Regime 2")
plt.xlabel("omega")
plt.ylabel("average page faults")

plt.legend()
plt.show()

# trend3-regime1
df = pd.read_csv('/content/sample_data/trend3-regime1.csv')
df_sorted = df.sort_values('epsilon', ascending=True)

plt.plot(df['epsilon'], df['pageFaultsOfOpt'], label = "pageFaultsOfOpt")
plt.plot(df['epsilon'], df['pageFaultsOfBlindOracle'], label = "pageFaultsOfBlindOracle")
plt.plot(df['epsilon'], df['pageFaultsOfLRU'], label = "pageFaultsOfLRU")
plt.plot(df['epsilon'], df['pageFaultsOfCombined'], label = "pageFaultsOfCombined")

plt.title("Page faults of algorithms over varying epsilon -- Regime 1")
plt.xlabel("epsilon")
plt.ylabel("average page faults")

plt.legend()
plt.show()

# trend3-regime2
df = pd.read_csv('/content/sample_data/trend3-regime2.csv')
df_sorted = df.sort_values('epsilon', ascending=True)

plt.plot(df['epsilon'], df['pageFaultsOfOpt'], label = "pageFaultsOfOpt")
plt.plot(df['epsilon'], df['pageFaultsOfBlindOracle'], label = "pageFaultsOfBlindOracle")
plt.plot(df['epsilon'], df['pageFaultsOfLRU'], label = "pageFaultsOfLRU")
plt.plot(df['epsilon'], df['pageFaultsOfCombined'], label = "pageFaultsOfCombined")

plt.title("Page faults of algorithms over varying epsilon -- Regime 2")
plt.xlabel("epsilon")
plt.ylabel("average page faults")

plt.legend()
plt.show()

# trend4-regime1
df = pd.read_csv('/content/sample_data/trend4-regime1.csv')
df_sorted = df.sort_values('tow', ascending=True)

plt.plot(df['tow'], df['pageFaultsOfOpt'], label = "pageFaultsOfOpt")
plt.plot(df['tow'], df['pageFaultsOfBlindOracle'], label = "pageFaultsOfBlindOracle")
plt.plot(df['tow'], df['pageFaultsOfLRU'], label = "pageFaultsOfLRU")
plt.plot(df['tow'], df['pageFaultsOfCombined'], label = "pageFaultsOfCombined")

plt.title("Page faults of algorithms over varying tow -- Regime 1")
plt.xlabel("tow")
plt.ylabel("average page faults")

plt.legend()
plt.show()

# trend4-regime2
df = pd.read_csv('/content/sample_data/trend4-regime2.csv')
df_sorted = df.sort_values('tow', ascending=True)

plt.plot(df['tow'], df['pageFaultsOfOpt'], label = "pageFaultsOfOpt")
plt.plot(df['tow'], df['pageFaultsOfBlindOracle'], label = "pageFaultsOfBlindOracle")
plt.plot(df['tow'], df['pageFaultsOfLRU'], label = "pageFaultsOfLRU")
plt.plot(df['tow'], df['pageFaultsOfCombined'], label = "pageFaultsOfCombined")

plt.title("Page faults of algorithms over varying tow -- Regime 2")
plt.xlabel("tow")
plt.ylabel("average page faults")

plt.legend()
plt.show()
